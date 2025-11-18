package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.utils.estructuraDatos.TrieAutocompletado;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.CancionBusquedaService;
import co.edu.uniquindio.utils.listasPropias.MiLinkedList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz {@link CancionBusquedaService} para búsquedas avanzadas y autocompletado de canciones.
 *
 * <p>Utiliza un Árbol de Prefijos (Trie) para búsquedas rápidas en memoria y {@link Specification} de Spring Data JPA
 * para el filtrado dinámico en la base de datos, con ejecución asíncrona.</p>
 *
 */
@Service
@RequiredArgsConstructor
public class CancionBusquedaServiceImpl implements CancionBusquedaService {

    // Inyección de dependencias.
    private final CancionMapper cancionMapper;
    private final CancionRepo cancionRepo;
    private final TrieAutocompletado trieCanciones = new TrieAutocompletado();



    /**
     * Método auxiliar para asegurar que el Trie (Árbol de Prefijos) esté cargado con todos los títulos de canciones.
     *
     * <p>Este método se ejecuta condicionalmente solo si el Trie está vacío, garantizando
     * que la carga desde la base de datos solo se haga una vez durante el ciclo de vida de la aplicación.</p>
     */
    private void inicializarTrie() {
        // Verifica si el Trie está vacío realizando una búsqueda de prefijo vacío.
        if (trieCanciones.autocompletar("").isEmpty()) {
            // Si está vacío, se obtienen todos los títulos de la base de datos.
            List<Cancion> canciones = cancionRepo.findAll();
            // Se inserta cada título de canción en el Trie.
            for (Cancion c : canciones) {
                trieCanciones.insertar(c.getTitulo());
            }
        }
    }

    /**
     * **Devuelve una lista de canciones cuyos títulos coincidan con el prefijo dado, utilizando el Trie.**
     *
     * @param prefijo Texto parcial introducido por el usuario (por ejemplo "ama").
     * @return Lista de {@link CancionDto} que comienzan con ese prefijo.
     */
    @Override
    public List<CancionDto> autocompletarTitulos(String prefijo) {
        // Aseguramos que el Trie esté inicializado y cargado antes de realizar cualquier búsqueda.
        inicializarTrie();

        // 1. Obtenemos las coincidencias de títulos exactos a partir del Trie (operación rápida en memoria).
        var coincidenciasMiLista = trieCanciones.autocompletar(prefijo);

        // 1. Obtenemos las coincidencias de títulos exactos a partir del Trie (operación rápida en memoria).
        // Convertir MiLinkedList → List de Java para usar Spring Data.
        List<String> coincidencias = convertirMiLinkedList(coincidenciasMiLista);


        // 2. Si no hay coincidencias de títulos, devolvemos una lista de DTOs vacía.
        if (coincidencias.isEmpty()) {
            return List.of();
        }

        // 3. Consultamos las canciones en la base de datos, buscando aquellas cuyo título esté en la lista de coincidencias.
        List<Cancion> canciones = cancionRepo.findByTituloInIgnoreCase(coincidencias);

        // 4. Convertimos las entidades a DTO antes de devolver el resultado.
        return canciones.stream()
                .map(cancionMapper::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Convierte una lista enlazada propia (MiLinkedList) de Strings a una lista estándar de Java (ArrayList).
     *
     * <p>Esta conversión es necesaria para interactuar con las APIs de Java y Spring Data
     * que esperan colecciones estándar.</p>
     *
     * @param lista La {@link MiLinkedList} de nombres artísticos a convertir.
     * @return Una {@link java.util.List} de Strings con los mismos elementos.
     */
    private List<String> convertirMiLinkedList(MiLinkedList<String> lista) { // Método auxiliar para la conversión de tipos de lista.
        List<String> resultado = new ArrayList<>(); // Inicializa una lista estándar (ArrayList) para el resultado.

        for (String s : lista) { // Itera sobre la MiLinkedList usando su implementación de Iterable.
            resultado.add(s); // Añade cada String de la lista propia a la lista estándar.
        }

        return resultado; // Retorna la lista estándar de Java.
    }


    /**
     * Busca canciones aplicando filtros dinámicos (Artista, Género, Año) y devuelve el resultado de forma asíncrona.
     *
     * <p>Utiliza Spring Data JPA {@link Specification} para construir consultas dinámicas
     * y las ejecuta en un hilo separado gracias a {@code @Async}.</p>
     *
     * @param artista Nombre del artista para filtrar (opcional).
     * @param genero Género musical para filtrar (opcional).
     * @param anioLanzamiento Año de lanzamiento para filtrar (opcional).
     * @param pagina El número de página de resultados a retornar.
     * @param size El tamaño de la página.
     * @return Un {@code CompletableFuture} que contendrá la lista de canciones filtradas.
     */
    @Override
    @Async // Indica que este método se ejecutará en un hilo de ejecución separado (asíncrono).
    public CompletableFuture<List<CancionDto>> listarCancionesFiltro(
            String artista, String genero, Integer anioLanzamiento, int pagina, int size) {

        // 1. Configurar el objeto de paginación con el número de página y el tamaño.
        Pageable pageable = PageRequest.of(pagina, size);

        // 2. Inicializar las especificaciones para las condiciones AND y OR.
        // Specification.where(null) crea una especificación base que no filtra nada.
        Specification<Cancion> andSpec = Specification.where(null);
        Specification<Cancion> orSpec = Specification.where(null);


        // 3. Filtro por artista
        if (artista != null && !artista.isEmpty()) {
            // Define el criterio: root.get("artista") debe ser igual al valor del parámetro.
            Specification<Cancion> artistaSpec = (root, query, builder) ->
                    builder.equal(root.get("artistaPrincipal").get("id"), Long.parseLong(artista));
            // Combina el nuevo filtro a la especificación AND.
            andSpec = andSpec.and(artistaSpec);
            // Combina el nuevo filtro a la especificación OR.
            orSpec = orSpec.or(artistaSpec);
        }

        // 4. Construir el filtro por género.
        if (genero != null && !genero.isEmpty()) {
            // Define el criterio: root.get("genero") debe ser igual al valor del parámetro.
            Specification<Cancion> generoSpec = (root, query, builder) ->
                    builder.equal(root.get("generoMusical"), genero);
            // Combina el nuevo filtro a la especificación AND.
            andSpec = andSpec.and(generoSpec);
            // Combina el nuevo filtro a la especificación OR.
            orSpec = orSpec.or(generoSpec);
        }

        // 5. Filtro por año de lanzamiento
        // Filtro por año de lanzamiento
        if (anioLanzamiento != null) {

            int anio = Integer.parseInt(String.valueOf(anioLanzamiento));

            LocalDate inicio = LocalDate.of(anio, 1, 1);
            LocalDate fin = LocalDate.of(anio, 12, 31);

            Specification<Cancion> anioSpec = (root, query, builder) ->
                    builder.between(root.get("fechaLanzamiento"), inicio, fin);

            andSpec = andSpec.and(anioSpec);
            orSpec = orSpec.or(anioSpec);
        }


        // 6. Ejecutar la consulta para canciones que cumplan *todos* los filtros (AND).
        List<Cancion> cancionesAnd = cancionRepo.findAll(andSpec, pageable).getContent();

        // 7. Ejecutar la consulta para canciones que cumplan *alguno* de los filtros (OR).
        List<Cancion> cancionesOr = cancionRepo.findAll(orSpec, pageable).getContent();

        // 8. Combinar los resultados de AND y OR en un Set (LinkedHashSet) para eliminar duplicados
        // y mantener el orden de inserción.
        Set<Cancion> resultadoFinal = new LinkedHashSet<>(cancionesAnd);
        resultadoFinal.addAll(cancionesOr);

        // 9. Mapear la colección final de entidades a una lista de DTOs.
        List<CancionDto> cancionesDto = resultadoFinal.stream()
                .map(cancionMapper::toDto)
                .collect(Collectors.toList());

        // 10. Retornar el resultado envuelto en un CompletableFuture completado,
        // cumpliendo con el contrato del método asíncrono.
        return CompletableFuture.completedFuture(cancionesDto);
    }

}
