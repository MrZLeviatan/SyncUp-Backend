package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
     * Obtiene todas las canciones registradas en el sistema.
     *
     * @return DTO con la lista de la canciones generales
     */
    List<CancionDto> listarCancionesGeneral();


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



    // Búsqueda de canciones


    /**
     * Proporciona sugerencias de autocompletado para títulos de canciones utilizando un Árbol de Prefijos (Trie).
     *
     * <p>Esta función es síncrona y rápida, ya que opera sobre una estructura de datos en memoria.</p>
     *
     * @param prefijo La cadena de texto (parcial) que el usuario ha ingresado.
     * @return Una lista de {@link CancionDto}s con los títulos completos que coinciden con el prefijo dado.
     */
    List<CancionDto> autocompletarTitulos(String prefijo);



    /**
     * Busca canciones aplicando diversos filtros y ejecuta la operación de manera asíncrona.
     *
     * <ul>
     * <li>`@Async`: Indica que este método debe ejecutarse en un *hilo de ejecución separado*
     * (fuera del hilo principal de la petición web). Esto evita bloquear la respuesta HTTP
     * mientras la base de datos realiza la consulta, mejorando la escalabilidad del sistema.</li>
     *
     * <li>`CompletableFuture<List<CancionDto>>`: Es un contenedor que representa un *resultado que estará disponible en el futuro*.
     * Dado que el método se ejecuta en segundo plano (`@Async`), no puede devolver la lista de inmediato.
     * En su lugar, devuelve este `CompletableFuture`, que eventualmente contendrá la `List<CancionDto>`
     * cuando la consulta a la base de datos finalice. El controlador o el código que llama a este servicio
     * puede continuar su ejecución y luego "esperar" o reaccionar a la finalización de esta operación futura.</li>
     * </ul>
     *
     * @param artista Nombre del artista para filtrar (opcional).
     * @param genero Género musical para filtrar (opcional).
     * @param anioLanzamiento Año de lanzamiento para filtrar (opcional).
     * @param pagina El número de página de resultados a retornar (para paginación).
     * @param size El tamaño de la página (cantidad de resultados por página).
     * @return Un {@code CompletableFuture} que contendrá la lista de canciones filtradas.
     */
    @Async
    CompletableFuture<List<CancionDto>> listarCancionesFiltro(
            String artista, String genero, Integer anioLanzamiento, int pagina, int size);



    // -- Archivos Canciones

    /**
     * Genera un reporte de las canciones favoritas de un usuario en formato CSV en memoria.
     *
     * <p>El reporte es construido como un flujo de bytes para ser leído y descargado directamente.</p>
     *
     * @param usuarioId ID del usuario del que se generará el reporte de canciones favoritas.
     * @return Un {@code ByteArrayInputStream} que contiene el contenido completo del archivo CSV.
     * @throws ElementoNoEncontradoException Si el usuario con el ID especificado no se encuentra en el sistema.
     * @throws Exception Para errores de Entrada/Salida (E/S) durante la construcción del archivo o cualquier otro error inesperado.
     */
    ByteArrayInputStream generarReporteFavoritos(Long usuarioId) throws ElementoNoEncontradoException, Exception;


    /**
     * Genera un reporte general de todas las canciones registradas en el sistema en formato de texto plano (TXT).
     *
     * <p>Recupera todas las canciones de la base de datos y formatea sus metadatos clave para su exportación.</p>
     *
     * @return Un {@code ByteArrayInputStream} que contiene el contenido completo del archivo TXT.
     * @throws Exception Para errores de Entrada/Salida (E/S) durante la construcción del archivo o cualquier otro error inesperado.
     */
    ByteArrayInputStream generarReporteGeneralCanciones() throws Exception;


}
