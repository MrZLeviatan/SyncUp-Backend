package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import co.edu.uniquindio.utils.estructuraDatos.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.ArtistaRepo;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.CancionService;
import co.edu.uniquindio.utils.CloudinaryUtils;
import co.edu.uniquindio.utils.estructuraDatos.TrieAutocompletado;
import co.edu.uniquindio.utils.listasPropias.MiLinkedList;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.mpatric.mp3agic.Mp3File;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * Implementación de la interfaz {@link CancionService} que contiene la lógica de negocio para la gestión de canciones.
 *
 * <p>Este servicio maneja la creación, actualización, eliminación de canciones,
 * la integración con servicios externos (Cloudinary) y la actualización del
 * grafo de similitud para recomendaciones.</p>
 */
@Service
@RequiredArgsConstructor
public class CancionServiceImpl implements CancionService {

    // Repositorios y utilidades inyectada vía Lombok's @RequiredArgsConstructor
    private final CancionRepo cancionRepo;
    private final CancionMapper cancionMapper;
    private final ArtistaRepo artistaRepo;
    private final UsuarioRepo usuarioRepo;

    // Componente para la lógica de guardado de Imágenes y Canciones en la nube
    private final CloudinaryUtils cloudinaryUtils;
    // Componente para la lógica de recomendación (grafo)
    private final GrafoDeSimilitud grafoDeSimilitud;

    private final TrieAutocompletado trieCanciones;


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
     * Procesa el registro de una nueva canción, incluyendo la subida de archivos y la actualización del grafo de similitud.
     *
     * @param registrarCancionDto DTO con los datos y archivos de la nueva canción.
     * @throws ElementoNoEncontradoException Si el artista principal no existe.
     * @throws ElementoNoValidoException Si los archivos superan el tamaño máximo permitido.
     * @throws IOException Si ocurre un error de I/O durante la manipulación de archivos.
     * @throws InvalidDataException Si el archivo MP3 es inválido.
     * @throws UnsupportedTagException Si el archivo MP3 tiene etiquetas no soportadas.
     */
    @Override
    public void agregarCancion(RegistrarCancionDto registrarCancionDto) throws ElementoNoEncontradoException, ElementoNoValidoException,
            IOException, InvalidDataException, UnsupportedTagException {

        // 1. Se busca el artista mediante su ID
        Artista artista = artistaRepo.findById(registrarCancionDto.artistaId())
                .orElseThrow(()-> new ElementoNoEncontradoException("Artista no  encontrado"));

        // 2, Se sube la imagen de la porta y se recibe su URL
        String urlImage = cloudinaryUtils.uploadImage(registrarCancionDto.imagenPortada());

        // 3. Se sube la canción y se recibe su URL
        MultipartFile archivoMp3 = registrarCancionDto.archivoCancion();
        String urlMp3 = cloudinaryUtils.uploadMp3(registrarCancionDto.archivoCancion());

        // 4. Calcular la duración de la canción desde mp3agic
            // Convertir MultipartFile a File temporal
            File mp3Temporal = File.createTempFile(archivoMp3.getOriginalFilename(), null);
            try (FileOutputStream fos = new FileOutputStream(mp3Temporal)) {
                fos.write(archivoMp3.getBytes());
            }

            // Se obtiene la duración del archivo MP3
            Mp3File mp3File = new Mp3File(mp3Temporal);
            long duracionSegundos = mp3File.getLengthInSeconds();
            String duracion = String.format("%02d:%02d", duracionSegundos / 60, duracionSegundos % 60);

        // Eliminar archivo temporal
        mp3Temporal.delete();

        // 5. Se mapea la canción mediante su DTO
        Cancion cancion = cancionMapper.toEntity(registrarCancionDto);

        // Se asignan los datos faltantes
        cancion.setUrlCancion(urlMp3);
        cancion.setUrlPortada(urlImage);
        cancion.setDuracion(duracion);

        // Usar el método del modelo Artista para mantener la relación bidireccional
        artista.agregarCancion(cancion);

        // 6. Guardar en base de datos
        cancionRepo.save(cancion);
        artistaRepo.save(artista);

        // === Sincronización con el grafo ===

        // Agregar la nueva canción como nodo
        grafoDeSimilitud.agregarCancion(cancion);

        // Conectar esta canción con todas las demás en el grafo
        for (Cancion otra : cancionRepo.findAll()) {
            if (!otra.equals(cancion)) {
                double peso = calcularPesoSimilitud(cancion, otra);
                grafoDeSimilitud.conectarCanciones(cancion, otra, peso);
            }
        }
    }

    /**
     * Calcula el peso de similitud entre dos canciones.
     *
     * @param c1 primera canción
     * @param c2 segunda canción
     * @return peso (menor = más similar)
     */
    private double calcularPesoSimilitud(Cancion c1, Cancion c2) {
        double similitud = 0.0;

        // Se calcula de esta forma para que no repitan los artistas.
        if (c1.getGeneroMusical().equals(c2.getGeneroMusical())) similitud += 0.6;
        if (c1.getArtistaPrincipal().equals(c2.getArtistaPrincipal())) similitud += 0.4;

        return 1 - similitud; // Convert similarity to cost / Convierte similitud a costo

    }


    /**
     * Actualiza los metadatos de una canción existente.
     *
     * @param editarCancionDto DTO con el ID y los nuevos metadatos.
     * @throws ElementoNoEncontradoException Si la canción a editar no existe.
     */
    @Override
    public void actualizarCancion(EditarCancionDto editarCancionDto) throws ElementoNoEncontradoException {


        // 1. Se busca la canción actualizar
        Cancion cancion = buscarCancionId(editarCancionDto.id());

        // 2. Remover del grafo antes de actualizar (para evitar pesos viejos)
        grafoDeSimilitud.eliminarCancion(cancion);

        // 3. Se mapea la canción actualizar
        cancionMapper.updateCancionFromDto(editarCancionDto, cancion);

        // 3. Se guarda en la base de datos
        cancionRepo.save(cancion);

        // 4. Volver a agregarla al grafo
        grafoDeSimilitud.agregarCancion(cancion);

        // 5. Reconectar con todas las demás canciones y recalcular pesos
        for (Cancion otra : cancionRepo.findAll()) {
            if (!otra.equals(cancion)) {
                double peso = calcularPesoSimilitud(cancion, otra);
                grafoDeSimilitud.conectarCanciones(cancion, otra, peso);
            }
        }

    }


    /**
     * Elimina una canción del sistema y la remueve del grafo de similitud.
     *
     * @param idCancion ID de la canción a eliminar.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    @Override
    @Transactional
    public void eliminarCancion(Long idCancion) throws ElementoNoEncontradoException {

        // 1. Obtener la canción
        Cancion cancion = buscarCancionId(idCancion);

        // 2. Romper relación con el artista
        Artista artista = cancion.getArtistaPrincipal();
        if (artista != null) {
            artista.getCanciones().remove(cancion);
            cancion.setArtistaPrincipal(null);
        }

        // 3. Eliminar la canción de las listas de favoritos de los usuarios
        List<Usuario> usuarios = usuarioRepo.findAll();
        for (Usuario u : usuarios) {
            if (u.getCancionesFavoritas().contains(cancion)) {
                u.getCancionesFavoritas().remove(cancion);
            }
        }

        // 4. Eliminar del grafo de similitud
        grafoDeSimilitud.eliminarCancion(cancion);

        // 5. Borrar archivos Cloudinary
        cloudinaryUtils.borrarArchivo(cancion.getUrlCancion());
        cloudinaryUtils.borrarArchivo(cancion.getUrlPortada());

        // 6. Eliminar la canción
        cancionRepo.delete(cancion);
    }



    /**
     * Obtiene la información de una canción específica.
     *
     * @param idCancion ID de la canción a obtener.
     * @return DTO con la información de la canción.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    @Override
    public CancionDto obtenerCancion(Long idCancion) throws ElementoNoEncontradoException {
        // Busca la entidad y la mapea a DTO
        return cancionMapper.toDto(buscarCancionId(idCancion));
    }

    @Override
    public List<CancionDto> listarCancionesGeneral() {

        List<Cancion> listasCanciones = cancionRepo.findAll();

        return listasCanciones.stream()
                .map(cancionMapper::toDto)
                .toList(); // Retorna la lista final de canciones en formato DTO
    }


    /**
     * Lista las canciones marcadas como favoritas por un usuario específico.
     *
     * @param idUsuario ID del usuario.
     * @return Lista de {@link CancionDto}s.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     */
    @Override
    public List<CancionDto> listarCancionesFavoritasUsuario(Long idUsuario) throws ElementoNoEncontradoException {

        // 1. Se obtienen las canciones favoritas del usuario (que se obtiene mediante su ID)
        List<Cancion> cancionesFavoritas = buscarUsuarioId(idUsuario).getListaCancionesFavoritas();

        // 2. Se mapea al objeto DTO y se devuelve la lista.
        return cancionesFavoritas.stream().map(cancionMapper::toDto).toList();
    }


    /**
     * Agrega una canción a la lista de favoritas de un usuario.
     *
     * @param idUsuario ID del usuario que agrega la canción.
     * @param idCancion ID de la canción a agregar.
     * @throws ElementoNoEncontradoException Si el usuario o la canción no existen.
     */
    @Override
    public void agregarCancionFavoritaUsuario(Long idUsuario, Long idCancion) throws ElementoNoEncontradoException {

        // 1. Se busca la canción a agregar a favoritas
        Cancion cancion = buscarCancionId(idCancion);

        // 2. Se busca el usuario que agregara la canción
        Usuario usuario = buscarUsuarioId(idUsuario);

        // 3. Se agrega la canción a la lista del usuario
        usuario.agregarCancionFavorita(cancion);

        // 4. Guardar los cambios para persistir la relación en la tabla intermedia
        usuarioRepo.save(usuario);
    }

    /**
     * Eliminar una canción de la lista de favoritas de un usuario.
     *
     * @param idUsuario ID del usuario que elimina la canción.
     * @param idCancion ID de la canción a eliminar.
     * @throws ElementoNoEncontradoException Si el usuario o la canción no existen.
     */
    @Override
    public void quitarCancionFavoritaUsuario(Long idUsuario, Long idCancion) throws ElementoNoEncontradoException {
        // Buscar el usuario en base de datos
        Usuario usuario = buscarUsuarioId(idUsuario);

        // Buscar la canción a eliminar
        Cancion cancion = buscarCancionId(idCancion);

        // Verificar si la canción está en la lista de favoritas
        if (!usuario.getListaCancionesFavoritas().contains(cancion)) {
            throw new ElementoNoEncontradoException("La canción no está en la lista de favoritas del usuario");
        }

        // Quitar la canción de la lista en memoria
        usuario.eliminarCancionFavorita(cancion);

        // Guardar el usuario actualizado para persistir el cambio en la tabla intermedia
        usuarioRepo.save(usuario);
    }


    /**
     * Método auxiliar para buscar y retornar una entidad Usuario por ID.
     *
     * @param idUsuario ID del usuario a buscar.
     * @return Entidad {@link Usuario} encontrada.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     */
    private Usuario buscarUsuarioId(Long idUsuario) throws ElementoNoEncontradoException {
        return usuarioRepo.findById(idUsuario).orElseThrow(() -> new ElementoNoEncontradoException("Usuario no encontrado"));
    }


    /**
     * Método auxiliar para buscar y retornar una entidad Cancion por ID.
     *
     * @param idCancion ID de la canción a buscar.
     * @return Entidad {@link Cancion} encontrada.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    private Cancion buscarCancionId(Long idCancion) throws ElementoNoEncontradoException {
        return cancionRepo.findById(idCancion).orElseThrow(()-> new ElementoNoEncontradoException("Canción no encontrada"));
    }


    /**
     * Obtiene métricas del sistema basadas en las canciones registradas.
     *
     * <p>Calcula y retorna un mapa con varias métricas clave, incluyendo:</p>
     * <ul>
     * <li>{@code totalCanciones}: Cantidad total de canciones.</li>
     * <li>{@code cancionesPorGenero}: Conteo de canciones agrupadas por {@code GeneroMusical}.</li>
     * <li>{@code artistaTop}: Nombre artístico del artista con más canciones registradas.</li>
     * </ul>
     *
     * @return Un {@code Map<String, Object>} con todas las métricas calculadas.
     */
    @Override
    public Map<String, Object> obtenerMetricasCanciones() {
        // Se obtienen todas las canciones desde la base de datos usando el repositorio cancionRepo.
        List<Cancion> canciones = cancionRepo.findAll();
        // Se crea un mapa (HashMap) donde se almacenarán las métricas calculadas.
        // Cada entrada del mapa tendrá un nombre de métrica como clave (por ejemplo "totalCanciones") y su valor correspondiente.
        Map<String, Object> metricas = new HashMap<>();

        // Se cuenta el total de canciones registradas (canciones.size()).
        // Se guarda este número en el mapa con la clave "totalCanciones".
        metricas.put("totalCanciones", canciones.size());

        // Se usa Stream API para agrupar las canciones por su género musical. groupingBy(...) crea un mapa donde la clave es el nombre del género
        // El resultado es un Map<String, Long> que indica cuántas canciones hay por cada género.
        Map<String, Long> porGenero = canciones.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getGeneroMusical().toString(),
                        Collectors.counting()
                ));
        // Se guarda el resultado anterior dentro del mapa principal metricas bajo la clave "cancionesPorGenero".
        metricas.put("cancionesPorGenero", porGenero);

        // Nuevamente, se usa Stream API, pero esta vez agrupando por el nombre artístico del artista principal.
        // Se cuentan cuántas canciones tiene cada artista.
        Map<String, Long> porArtista = canciones.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getArtistaPrincipal().getNombreArtistico(),
                        Collectors.counting()
                ));
        // Se busca el artista con mayor cantidad de canciones (max sobre el valor del mapa).
        porArtista.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                // Si existe, se toma su nombre (e.getKey()) y se guarda en el mapa de métricas bajo la clave "artistaTop".
                .ifPresent(e -> metricas.put("artistaTop", e.getKey()));

        return metricas;
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


    /**
     * Genera un CSV con las canciones favoritas del usuario identificado por usuarioId.
     *
     * @param usuarioId id del usuario
     * @return ByteArrayInputStream con el contenido del CSV
     * @throws ElementoNoEncontradoException si el usuario no existe
     * @throws Exception en caso de errores de entrada/salida
     */
    @Override
    public ByteArrayInputStream generarReporteFavoritos(Long usuarioId) throws ElementoNoEncontradoException, Exception {

        // Buscar el usuario por su ID en la base de datos; si no existe, lanzar excepción.
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new ElementoNoEncontradoException("Usuario no encontrado con id: " + usuarioId));

        // Obtener la lista de canciones favoritas del usuario.
        List<Cancion> favoritas = usuario.getListaCancionesFavoritas();

        // Crear un buffer en memoria para generar el CSV (salida binaria).
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Envolver el flujo binario en un escritor de texto para poder escribir líneas.
        // Usamos UTF-8 para soportar caracteres acentuados en los títulos/artistas.
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, "UTF-8"))) {

            // Escribir la primera línea con los encabezados del CSV.
            writer.write("ID;Titulo;Genero;FechaLanzamiento;Artista;Duracion");
            writer.newLine(); // Salto de línea después de los encabezados.

            // Iterar por cada canción favorita y escribir sus campos separados por ';'.
            for (Cancion c : favoritas) {
                // Preparar cada campo (evitar nulos)
                String id = c.getId() == null ? "" : c.getId().toString();
                String titulo = c.getTitulo() == null ? "" : c.getTitulo();
                String genero = c.getGeneroMusical() == null ? "" : c.getGeneroMusical().toString();
                String fecha = c.getFechaLanzamiento() == null ? "" : c.getFechaLanzamiento().toString();
                String artista = (c.getArtistaPrincipal() == null || c.getArtistaPrincipal().getNombreArtistico() == null)
                        ? "" : c.getArtistaPrincipal().getNombreArtistico();
                String duracion = c.getDuracion() == null ? "" : c.getDuracion();

                // Escribir la línea formateada con delimitador ';'
                writer.write(String.join(";", id, titulo, genero, fecha, artista, duracion));
                writer.newLine(); // Nueva línea para la siguiente canción.
            }

            // Forzar el vaciado del buffer al flujo subyacente.
            writer.flush();
        } catch (IOException e) {
            // Si ocurre un error de E/S, envolver la excepción y re-lanzarla.
            throw new Exception("Error al generar el CSV: " + e.getMessage(), e);
        }

        // Crear un ByteArrayInputStream a partir de los bytes escritos en memoria
        // y retornarlo para que el controlador pueda leer los bytes y enviarlos en la respuesta.
        return new ByteArrayInputStream(baos.toByteArray());
    }



    /**
     * Genera un reporte general de todas las canciones registradas en el sistema en formato de texto plano (TXT).
     *
     * <p>Recupera todas las canciones de la base de datos y formatea sus metadatos clave
     * en un {@code ByteArrayInputStream} para su descarga como archivo.</p>
     *
     * @return Un {@code ByteArrayInputStream} que contiene el contenido del reporte TXT codificado en UTF-8.
     * @throws Exception Si ocurre un error durante la obtención de datos, la manipulación de flujos o la escritura del archivo.
     */
    @Override
    public ByteArrayInputStream generarReporteGeneralCanciones() throws Exception {

        // English: Get all songs from DB
        // Español: Obtener todas las canciones de la BD
        List<Cancion> canciones = cancionRepo.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, "UTF-8"))) {

            // English: Header
            // Español: Encabezado
            writer.write("============== REPORTE GENERAL DE CANCIONES ==============");
            writer.newLine();
            writer.write("Total de canciones: " + canciones.size());
            writer.newLine();
            writer.newLine();

            // English: Table header
            // Español: Encabezado de tabla
            writer.write(String.format(
                    "%-5s %-25s %-15s %-15s %-20s %-10s",
                    "ID", "Título", "Género", "Lanzamiento", "Artista", "Duración"
            ));
            writer.newLine();
            writer.write("--------------------------------------------------------------------------" +
                    "-------------------------");
            writer.newLine();

            // English: Write each song row
            // Español: Escribir cada fila de la canción
            for (Cancion c : canciones) {

                String id = c.getId() != null ? c.getId().toString() : "N/A";
                String titulo = c.getTitulo() != null ? c.getTitulo() : "N/A";
                String genero = c.getGeneroMusical() != null ? c.getGeneroMusical().toString() : "N/A";
                String fecha = c.getFechaLanzamiento() != null ? c.getFechaLanzamiento().toString() : "N/A";

                String artista = (c.getArtistaPrincipal() != null &&
                        c.getArtistaPrincipal().getNombreArtistico() != null)
                        ? c.getArtistaPrincipal().getNombreArtistico()
                        : "N/A";

                String duracion = c.getDuracion() != null ? c.getDuracion().toString() : "N/A";

                writer.write(String.format(
                        "%-5s %-25s %-15s %-15s %-20s %-10s",
                        id, titulo, genero, fecha, artista, duracion
                ));
                writer.newLine();
            }

            writer.flush();

        } catch (IOException e) {
            throw new Exception("Error al generar archivo TXT: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }


}
