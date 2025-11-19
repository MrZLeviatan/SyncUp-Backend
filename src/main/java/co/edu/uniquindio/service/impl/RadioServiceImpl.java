package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.playList.PlayListDto;
import co.edu.uniquindio.dto.playList.RadioDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.utils.estructuraDatos.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.RadioService;
import co.edu.uniquindio.utils.listasPropias.MiLinkedList;
import co.edu.uniquindio.utils.listasPropias.MiMap;
import co.edu.uniquindio.utils.listasPropias.MiSet;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación concreta de la interfaz {@link RadioService}.
 *
 * <p>Contiene la lógica de negocio para generar colas de radio y listas de reproducción
 * personalizadas, utilizando el {@link GrafoDeSimilitud}.
 *
 * <p>Utiliza Spring Data JPA ({@link CancionRepo}) para la búsqueda inicial de entidades y MapStruct
 * ({@link CancionMapper}) para la transformación de entidades a DTOs.
 *
 * @see RadioService
 * @see GrafoDeSimilitud
 */
@Service
@RequiredArgsConstructor
public class RadioServiceImpl implements RadioService {

    // Servicio que proporciona acceso al grafo de similitud precálculo
    private final GrafoDeSimilitud grafo;

    /** Repositorio para buscar entidades {@link Cancion} en la base de datos. */
    private final CancionRepo cancionRepo;

    /** Repositorio para buscar entidades {@link Usuario} en la base de datos. */
    private final UsuarioRepo usuarioRepo;

    /** Mapper para convertir entidades {@link Cancion} a DTOs {@link CancionDto}. */
    private final CancionMapper cancionMapper;


    /**
     * Inicializa el grafo de similitud después de que Spring cargue el bean.
     */
    @PostConstruct
    public void inicializarGrafo() {

        List<Cancion> canciones = cancionRepo.findAll();

        // 1. Agregar todas las canciones como nodos
        canciones.forEach(grafo::agregarCancion);

        // 2. Conectar todos los pares con un peso basado en similitud
        for (Cancion c1 : canciones) {
            for (Cancion c2 : canciones) {
                if (!c1.equals(c2)) {
                    double peso = calcularPesoSimilitud(c1, c2);
                    grafo.conectarCanciones(c1, c2, peso);
                }
            }
        }
    }


    /**
     * Calcula el peso de similitud entre dos canciones.
     *
     * <p>Determina la afinidad entre dos canciones basándose en dos criterios:
     * género musical (mayor peso) y artista principal. El valor retornado es un *costo*
     * para el algoritmo de Dijkstra, donde un costo menor implica mayor similitud (mayor afinidad).</p>
     *
     * @param c1 primera canción
     * @param c2 segunda canción
     * @return peso El costo de la arista (menor = más similar) en el rango [0.0, 1.0].
     */
    private double calcularPesoSimilitud(Cancion c1, Cancion c2) { // Define el método que calcula el peso de la arista.

        double similitud = 0.0; // Inicializa la variable de similitud acumulada a cero.

        // Compara el género musical de ambas canciones.
        if (c1.getGeneroMusical().equals(c2.getGeneroMusical())) // Si los géneros son iguales:
            similitud += 0.6; // Añade 0.6 a la similitud (es el factor más importante).

        // Compara el artista principal de ambas canciones.
        if (c1.getArtistaPrincipal().equals(c2.getArtistaPrincipal())) // Si los artistas son iguales:
            similitud += 0.4; // Añade 0.4 a la similitud (es el factor secundario).

        // La similitud estará en el rango [0.0 (totalmente diferente), 1.0 (totalmente igual)].
        // Se invierte el valor para obtener el costo (peso de la arista) para el grafo:
        // 1 - Similitud. Si Similitud = 1.0 (iguales), Costo = 0.0 (camino más fácil).
        // Si Similitud = 0.0 (diferentes), Costo = 1.0 (camino más difícil).
        return 1 - similitud; // Convierte similitud a costo, donde un valor menor es mejor para el grafo.
    }


    /**
     * Inicia y genera una cola de reproducción tipo "Radio" a partir de una canción base.
     *
     * <p>La radio se genera extrayendo los 10 vecinos más similares de la canción base
     * directamente desde él {@link GrafoDeSimilitud}.
     *
     * @param cancionId El ID de la canción que sirve como punto de partida.
     * @return Un objeto {@link RadioDto} que contiene la cola de reproducción ordenada por similitud.
     * @throws ElementoNoEncontradoException Si la canción base no se encuentra en la base de datos.
     */
    @Override
    public RadioDto iniciarRadio(Long cancionId) throws ElementoNoEncontradoException {

        // 1. Busca la canción base en la base de datos usando su ID.
        Cancion cancionBase = cancionRepo.findById(cancionId)
                // Si no se encuentra, lanza una exceptión.
                .orElseThrow(()-> new ElementoNoEncontradoException("Canción con ID:" + cancionId + "No encontrada"));

        // 2. Obtiene los vecinos directos (usa MiMap) y los ordena por peso (costo).
        // Se usa el método modificado obtenerVecinos que retorna MiMap.
        MiMap<Cancion, Double> vecinos = grafo.obtenerVecinos(cancionBase);

        // Transforma los MiMap. Par en un Set de Map. Entry estándar de Java para poder usar Stream API.
        Set<Map.Entry<Cancion, Double>> entrySet = new HashSet<>();
        for (MiMap.Par<Cancion, Double> par : vecinos) {
            entrySet.add(Map.entry(par.key, par.value));
        }

        // Transforma los vecinos del mapa en una lista ordenada de canciones similares.
        List<CancionDto> similares = entrySet.stream()
                .sorted(Map.Entry.comparingByValue()) // Ordenar: Menor peso (costo) = Mayor similitud
                .limit(10)                 // Seleccionar las 10 mejores
                .map(Map.Entry::getKey)              // Extraer la Cancion
                .map(cancionMapper::toDto)           // Convierte cada Cancion a CancionDto usando el mapper
                .collect(Collectors.toList());

        // Crea un objeto RadioDto que contiene la canción base y las canciones similares
        return new RadioDto(cancionBase.getId(), similares);
    }


    /**
     * Genera una lista de descubrimiento semanal basada en las canciones favoritas del {@link Usuario}.
     *
     * <p>Utiliza Dijkstra para hallar los caminos más similares desde las favoritas hacia otras canciones.
     * Los resultados se acumulan en un
     * {@code Set} para asegurar que no haya canciones duplicadas en la recomendación final.
     *
     * @param idUsuario El objeto {@link Usuario} para el cual se generará la playlist.
     * @return Un objeto {@link PlayListDto} con las canciones recomendadas.
     */
    public PlayListDto generarDescubrimientoSemanal(Long idUsuario) throws ElementoNoEncontradoException {

        // Se busca al usuario mediante su Id
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(()-> new ElementoNoEncontradoException("Usuario no encontrado"));

        // Se obtiene las canciones favoritas del usuario
        List<Cancion> favoritas = usuario.getCancionesFavoritas();

        // Utiliza un Set estándar de Java para almacenar recomendaciones y evitar duplicados automáticamente.
        Set<Cancion> recomendadas = new HashSet<>();

        // Obtiene todas las canciones del grafo usando MiSet
        MiSet<Cancion> todasLasCanciones = grafo.obtenerCanciones();

        // Itera sobre cada canción favorita para encontrar sus "vecinos".
        // Por cada favorita, buscar las canciones más cercanas usando Dijkstra.
        for (Cancion favorita : favoritas) {
            // Esto permite comparar cada favorita con todas las demás canciones.
            // Se usa el iterador del MiSet.
            for (Cancion otra : todasLasCanciones) {
                // evita compararse consigo misma o no esté ya entre las favoritas del usuario.
                if (!favorita.equals(otra) && !favoritas.contains(otra)) {
                    // Se ejecuta el algoritmo de Dijkstra que retorna MiLinkedList.
                    MiLinkedList<Cancion> camino = grafo.dijkstra(favorita, otra);

                    // Si la lista tiene más de un elemento, significa que sí existe una ruta real.
                    if (camino.size() > 1) {
                        // Tomar la siguiente canción después de la favorita en el camino.
                        // Usamos el método get() de MiLinkedList.
                        Cancion siguiente = camino.get(1);
                        // Se agrega esa canción al conjunto de recomendaciones.
                        recomendadas.add(siguiente);
                    }
                }
            }
        }

        // Limitar a 5 recomendaciones máximo y evitar repetidos
        List<CancionDto> cancionesDTO = recomendadas.stream()
                .limit(5)
                .map(cancionMapper::toDto)
                .collect(Collectors.toList());

        return new PlayListDto("Descubrimiento Semanal", cancionesDTO);
    }
}
