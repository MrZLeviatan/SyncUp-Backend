package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.ArtistaRepo;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.CancionService;
import co.edu.uniquindio.service.utils.CloudinaryService;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mpatric.mp3agic.Mp3File;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final CloudinaryService cloudinaryService;
    // Componente para la lógica de recomendación (grafo)
    private final GrafoDeSimilitud grafoDeSimilitud;


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
        String urlImage = cloudinaryService.uploadImage(registrarCancionDto.imagenPortada());

        // 3. Se sube la canción y se recibe su URL
        MultipartFile archivoMp3 = registrarCancionDto.archivoCancion();
        String urlMp3 = cloudinaryService.uploadMp3(registrarCancionDto.archivoCancion());

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

        // 2. Se mapea la canción actualizar
        cancionMapper.updateCancionFromDto(editarCancionDto, cancion);

        // 3. Se guarda en la base de datos
        cancionRepo.save(cancion);
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
        cloudinaryService.borrarArchivo(cancion.getUrlCancion());
        cloudinaryService.borrarArchivo(cancion.getUrlPortada());

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

}
