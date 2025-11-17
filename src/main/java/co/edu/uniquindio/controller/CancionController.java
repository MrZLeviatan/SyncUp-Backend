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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;


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

}
