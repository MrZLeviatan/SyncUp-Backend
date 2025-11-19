package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import co.edu.uniquindio.service.CancionService;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * Controlador REST para la gestión completa de canciones y sus interacciones de favoritos.
 *
 * <p>Expone los *endpoints* para las operaciones CRUD básicas de canciones, así como para
 * la adición y eliminación de canciones de la lista de favoritos de un usuario.</p>
 *
 * @see CancionService
 */
@RestController
@RequestMapping("/api/cancion") // Prefijo base para todos los endpoints de este controlador.
@RequiredArgsConstructor
public class CancionController {

    // Inyección de la dependencia del servicio de canciones.
    private final CancionService cancionService;


    /**
     * Registra una nueva canción en el sistema.
     *
     * <p>Maneja la recepción de metadatos y archivos de audio/portada.</p>
     *
     * <p>Este endpoint recibe los datos y archivos del formulario (multipart/form-data)
     *    usando @ModelAttribute para manejar correctamente los archivos binarios.</p>
     *
     * @param dto DTO con los datos y archivos de la canción.
     * @return Mensaje de confirmación.
     * @throws ElementoNoEncontradoException Si el artista principal no existe.
     * @throws ElementoNoValidoException Si los archivos superan el límite de tamaño.
     * @throws IOException Si hay un error de I/O.
     * @throws InvalidDataException Si el archivo MP3 es inválido.
     * @throws UnsupportedTagException Si las etiquetas MP3 no son soportadas.
     */
    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN pueden registrar canciones
    public ResponseEntity<MensajeDto<String>> registrarCancion(
            @ModelAttribute RegistrarCancionDto dto)
            throws ElementoNoEncontradoException, ElementoNoValidoException,
            IOException, InvalidDataException, UnsupportedTagException {

        // Llama al servicio para registrar la canción y gestionar los archivos
        cancionService.agregarCancion(dto);

        // Retorna un mensaje de éxito con HTTP 200
        return ResponseEntity.ok().body(new MensajeDto<>(false, "Canción registrada exitosamente."));
    }


    /**
     * Actualiza los metadatos de una canción existente.**
     *
     * @param dto DTO con el ID de la canción y los campos a modificar.
     * @return Mensaje de confirmación.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN
    public ResponseEntity<MensajeDto<String>> actualizarCancion(@RequestBody EditarCancionDto dto)
            throws ElementoNoEncontradoException {
        // Llama al servicio para actualizar los metadatos de la canción.
        cancionService.actualizarCancion(dto);
        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false,"Canción actualizada exitosamente."));
    }


    /**
     * Elimina una canción del sistema.
     *
     * @param idCancion ID de la canción a eliminar, obtenido de la URL.
     * @return Mensaje de confirmación.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    @DeleteMapping("/eliminar/{idCancion}")
    @PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN
    public ResponseEntity<MensajeDto<String>> eliminarCancion(@PathVariable Long idCancion)
            throws ElementoNoEncontradoException {
        // Llama al servicio para eliminar la canción.
        cancionService.eliminarCancion(idCancion);
        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false, "Canción eliminada exitosamente"));
    }


    /**
     * Obtiene la información detallada de una canción por su ID.
     *
     * @param idCancion ID de la canción a buscar, obtenido de la URL.
     * @return {@code CancionDto} con la información de la canción.
     * @throws ElementoNoEncontradoException Si la canción no existe.
     */
    @GetMapping("/{idCancion}")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public ResponseEntity<MensajeDto<CancionDto>> obtenerCancion(@PathVariable Long idCancion)
            throws ElementoNoEncontradoException {
        // Llama al servicio para obtener el DTO de la canción.
        CancionDto cancion = cancionService.obtenerCancion(idCancion);
        // Retorna una respuesta 200 OK con el DTO encontrado.
        return ResponseEntity.ok().body(new MensajeDto<>(false, cancion));
    }


    /**
     * Listar todas las canciones generales
     *
     * @return Lista de {@code CancionDto} de las canciones.
     */
    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public ResponseEntity<MensajeDto<List<CancionDto>>> obtenerCancionesGeneral(){

        List<CancionDto> canciones = cancionService.listarCancionesGeneral();

        return ResponseEntity.ok().body(new MensajeDto<>(false,canciones));
    }


    /**
     * Lista todas las canciones marcadas como favoritas por un usuario específico.
     *
     * @param idUsuario ID del usuario, obtenido de la URL.
     * @return Lista de {@code CancionDto} de las canciones favoritas.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     */
    @GetMapping("/favoritas/{idUsuario}")
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<List<CancionDto>>> listarFavoritasUsuario(@PathVariable Long idUsuario)
            throws ElementoNoEncontradoException {
        // Llama al servicio para obtener la lista de DTOs de canciones favoritas.
        List<CancionDto> favoritas = cancionService.listarCancionesFavoritasUsuario(idUsuario);
        // Retorna una respuesta 200 OK con la lista de favoritos.
        return ResponseEntity.ok().body(new MensajeDto<>(false, favoritas));
    }


    /**
     * Agrega una canción a la lista de favoritas de un usuario.
     *
     * @param idUsuario ID del usuario que realiza la acción.
     * @param idCancion ID de la canción a agregar.
     * @return Mensaje de confirmación.
     * @throws ElementoNoEncontradoException Si el usuario o la canción no existen.
     */
    @PostMapping("/favoritas/{idUsuario}/agregar/{idCancion}")
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<String>> agregarFavorita(
            @PathVariable Long idUsuario,
            @PathVariable Long idCancion)
            throws ElementoNoEncontradoException {
        // Llama al servicio para establecer la relación de favorito.
        cancionService.agregarCancionFavoritaUsuario(idUsuario, idCancion);
        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false,"Canción agregada a favoritas"));
    }


    /**
     * Remueve una canción de la lista de favoritas de un usuario.
     *
     * @param idUsuario ID del usuario que realiza la acción.
     * @param idCancion ID de la canción a remover.
     * @return Mensaje de confirmación.
     * @throws ElementoNoEncontradoException Si el usuario o la canción no existen.
     */
    @DeleteMapping("/favoritas/{idUsuario}/quitar/{idCancion}")
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<String>> quitarFavorita(
            @PathVariable Long idUsuario,
            @PathVariable Long idCancion)
            throws ElementoNoEncontradoException {
        // Llama al servicio para eliminar la relación de favorito.
        cancionService.quitarCancionFavoritaUsuario(idUsuario, idCancion);
        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false, "Canción eliminada de favoritas"));
    }


    /**
     * Obtiene todas las métricas del sistema relacionadas con las canciones.
     *
     * <p>Este endpoint está restringido únicamente al rol de Administrador.</p>
     *
     * @return Un {@code ResponseEntity} conteniendo un {@code Map<String, Object>} con las métricas en formato JSON.
     */
    @GetMapping("/metricas")
    @PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN
    public ResponseEntity<Map<String, Object>> obtenerMetricas() { // La respuesta será un objeto HTTP con el mapa de métricas en formato JSON.
        // Devuelve una respuesta exitosa (código 200) con el cuerpo del JSON.
        return ResponseEntity.ok(cancionService.obtenerMetricasCanciones());
    }



    /**
     * Endpoint para obtener sugerencias de autocompletado de títulos de canciones.
     *
     * <p>Utiliza una estructura Trie en la capa de servicio para ofrecer una respuesta muy rápida.</p>
     *
     * @param prefijo Texto parcial del título a buscar, obtenido como parámetro de consulta.
     * @return Una respuesta HTTP 200 OK con una lista de {@code CancionDto}s coincidentes.
     */
    @GetMapping("/autocompletar")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public ResponseEntity<MensajeDto<List<CancionDto>>> autocompletarCanciones(@RequestParam String prefijo) {
        // Llama al servicio para ejecutar la búsqueda por prefijo (síncrona y rápida).
        List<CancionDto> resultados = cancionService.autocompletarTitulos(prefijo);
        // Retorna la respuesta 200 OK con los resultados envueltos en MensajeDto.
        return ResponseEntity.ok().body(new MensajeDto<>(false,resultados));
    }


    /**
     * Endpoint para listar y filtrar canciones mediante criterios y paginación, ejecutándose de forma asíncrona.
     *
     * <p>Retorna un {@code CompletableFuture}, delegando a Spring la gestión del hilo de ejecución
     * y permitiendo que la respuesta se complete cuando el resultado del servicio esté disponible.</p>
     *
     * @param artista Filtro opcional por nombre del artista.
     * @param genero Filtro opcional por género musical.
     * @param anioLanzamiento Filtro opcional por año de lanzamiento.
     * @param pagina El número de página solicitado (por defecto 0).
     * @param size El tamaño de la página (por defecto 10).
     * @return Un {@code CompletableFuture} que se resolverá en un {@code ResponseEntity<List<CancionDto>>}.
     */
    @GetMapping("/filtrar")
    //@PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public CompletableFuture<ResponseEntity<List<CancionDto>>> listarCancionesFiltro(
            @RequestParam(required = false) String artista, // Parámetro opcional.
            @RequestParam(required = false) String genero, // Parámetro opcional.
            @RequestParam(required = false) Integer anioLanzamiento, // Parámetro opcional.
            @RequestParam(defaultValue = "0") int pagina, // Parámetro con valor por defecto.
            @RequestParam(defaultValue = "10") int size) { // Parámetro con valor por defecto.

        // Llama al servicio, que retorna un CompletableFuture (la tarea se ejecuta en segundo plano).
        return cancionService.listarCancionesFiltro(artista, genero, anioLanzamiento, pagina, size)
                // Cuando el CompletableFuture del servicio se completa con la lista (List<CancionDto>),
                // la función thenApply la envuelve en un ResponseEntity 200 OK.
                .thenApply(ResponseEntity::ok);
    }


    /**
     * Endpoint que genera y devuelve un CSV con las canciones favoritas del usuario.
     * La respuesta HTTP es el propio archivo (el navegador inicia la descarga).
     *
     * @param usuarioId id del usuario cuyas canciones favoritas se exportarán
     * @return ResponseEntity con el CSV como bytes y cabeceras para descarga
     * @throws ElementoNoEncontradoException si el usuario no existe
     * @throws IOException en caso de error al leer los bytes del stream
     */
    @GetMapping("/reporte-favoritos/{usuarioId}")
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<byte[]> descargarReporteFavoritos(@PathVariable Long usuarioId)
            throws ElementoNoEncontradoException, IOException, Exception {

        // Llamar al servicio para generar el CSV en memoria y obtener un stream
        ByteArrayInputStream csvStream = cancionService.generarReporteFavoritos(usuarioId);

        // Leer todos los bytes del stream (contenido completo del CSV)
        byte[] csvBytes = csvStream.readAllBytes();

        // Preparar las cabeceras HTTP para indicar que la respuesta es un archivo descargable
        HttpHeaders headers = new HttpHeaders();
        // Indicar al navegador que guarde el contenido como un archivo con nombre "favoritos.csv"
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"favoritos.csv\"");
        // Indicar el tipo MIME del contenido como CSV
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        // Se puede añadir longitud de contenido (opcional)
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(csvBytes.length));

        // Devolver la respuesta con código 200 OK, cabeceras y el cuerpo binario (archivo)
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }



    /**
     * Endpoint que genera y devuelve un TXT con todas las canciones registradas en el sistema.
     *
     * <p>Recupera el reporte generado en formato {@code ByteArrayInputStream} desde la capa de servicio
     * y configura las cabeceras HTTP necesarias para forzar la descarga del archivo por parte del navegador.</p>
     *
     * @return {@code ResponseEntity<byte[]>} con el archivo TXT y cabeceras para descarga.
     * @throws Exception Sí ocurre un error durante la generación del reporte o la lectura de bytes.
     */
    @GetMapping("/reporte-general")
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden descargar el reporte general.
    public ResponseEntity<byte[]> descargarReporteGeneralCanciones() throws Exception {

        // Llamar al servicio para generar el reporte TXT y obtenerlo como un flujo de entrada en memoria.
        ByteArrayInputStream txtStream = cancionService.generarReporteGeneralCanciones();

        // Convertir el flujo de entrada (InputStream) a un arreglo de bytes ([]byte) para el cuerpo de la respuesta HTTP.
        byte[] txtBytes = txtStream.readAllBytes();

        // Preparar un objeto HttpHeaders para configurar las cabeceras de la respuesta.
        HttpHeaders headers = new HttpHeaders();
        // 1. Cabecera Content-Disposition: Indica al navegador que debe descargar el contenido y sugiere el nombre del archivo.
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_canciones.txt\"");
        // 2. Cabecera Content-Type: Especifica que el cuerpo de la respuesta es texto plano con codificación UTF-8.
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
        // 3. Cabecera Content-Length: Especifica el tamaño exacto del archivo en bytes.
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(txtBytes.length));

        // Retornar la respuesta final con el archivo.
        return ResponseEntity.ok()
                .headers(headers) // Asigna las cabeceras preparadas.
                .contentType(MediaType.parseMediaType("text/plain")) // Define el tipo de contenido nuevamente para claridad.
                .body(txtBytes); // Asigna el contenido del archivo al cuerpo de la respuesta.
    }


}
