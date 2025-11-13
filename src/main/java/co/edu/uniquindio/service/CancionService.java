package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * **Interfaz de servicio que define el contrato para todas las operaciones de negocio relacionadas con la entidad Canción.**
 *
 * <p>Esta capa de servicio es responsable de orquestar la lógica de negocio, incluyendo la
 * persistencia, validación de datos, manejo de archivos y la interacción con el grafo de similitud.</p>
 */
public interface CancionService {


    /**
     * Registra una nueva canción en el sistema.
     *
     * <p>El proceso incluye la subida de la portada y el archivo de audio a un servicio de almacenamiento
     * externo (ej. Cloudinary), la obtención de la duración y la sincronización con el grafo de similitud.</p>
     *
     * @param registrarCancionDto DTO con los metadatos y archivos binarios de la canción.
     * @throws ElementoNoEncontradoException Si el artista principal no existe.
     * @throws ElementoNoValidoException Si los archivos multimedia son inválidos o exceden el tamaño límite.
     * @throws IOException Si ocurre un error de I/O durante la manipulación de archivos.
     * @throws InvalidDataException Si el archivo MP3 es estructuralmente incorrecto.
     * @throws UnsupportedTagException Si el archivo MP3 contiene etiquetas de metadatos no soportadas.
     */
    void agregarCancion(RegistrarCancionDto registrarCancionDto)
            throws ElementoNoEncontradoException, ElementoNoValidoException, IOException,
            InvalidDataException, UnsupportedTagException;


    /**
     * Actualiza los metadatos de una canción existente.
     *
     * <p>Permite modificar campos como el título, género o fecha de lanzamiento.</p>
     *
     * @param editarCancionDto DTO con el ID de la canción y los campos a modificar.
     * @throws ElementoNoEncontradoException Si la canción a actualizar no existe.
     */
    void actualizarCancion(EditarCancionDto editarCancionDto)
            throws ElementoNoEncontradoException;

    /**
     * Elimina una canción del sistema de forma permanente.
     *
     * <p>También debe remover la canción del grafo de similitud y de cualquier otra lista o relación
     * asociada.</p>
     *
     * @param idCancion ID de la canción a eliminar.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    void eliminarCancion(Long idCancion)
            throws ElementoNoEncontradoException;


    /**
     * Obtiene la información detallada de una canción específica.
     *
     * @param idCancion ID de la canción a buscar.
     * @return DTO con la información de la canción.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    CancionDto obtenerCancion(Long idCancion)
            throws ElementoNoEncontradoException;


    /**
     * Lista todas las canciones que un usuario ha marcado como favoritas.
     *
     * @param idUsuario ID del usuario cuya lista de favoritos se desea consultar.
     * @return Una lista de {@link CancionDto}s.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     */
    List<CancionDto> listarCancionesFavoritasUsuario(Long idUsuario)
            throws ElementoNoEncontradoException;


    /**
     * Agrega una canción a la lista de favoritos de un usuario.
     *
     * @param idUsuario ID del usuario que realiza la acción.
     * @param idCancion ID de la canción a agregar.
     * @throws ElementoNoEncontradoException Si el usuario o la canción no existen.
     */
    void agregarCancionFavoritaUsuario(Long idUsuario, Long idCancion)
            throws ElementoNoEncontradoException;


    /**
     * Remueve una canción de la lista de favoritos de un usuario.
     *
     * @param idUsuario ID del usuario que realiza la acción.
     * @param idCancion ID de la canción a remover.
     * @throws ElementoNoEncontradoException Si el usuario o la canción no existen.
     */
    void quitarCancionFavoritaUsuario(Long idUsuario, Long idCancion)
            throws ElementoNoEncontradoException;


    /**
     * Obtiene métricas del sistema basadas en las canciones registradas.
     * Ejemplo: cantidad por género, artista más frecuente, total de canciones.
     * @return mapa con métricas
     */
    Map<String, Object> obtenerMetricasCanciones();


}
