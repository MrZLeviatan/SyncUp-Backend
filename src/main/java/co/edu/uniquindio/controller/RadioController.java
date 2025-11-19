package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.playList.PlayListDto;
import co.edu.uniquindio.dto.playList.RadioDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.service.RadioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de funcionalidades de recomendación de canciones.
 *
 * <p>Expone los *endpoints* relacionados con la generación dinámica de contenido
 * (ej. Radio por canción y Playlists de Descubrimiento) para los usuarios autenticados.</p>
 *
 */
@RestController
@RequestMapping("/api/recomendacion") // Prefijo base para todos los endpoints de este controlador.
@RequiredArgsConstructor
public class RadioController {

    // Inyección de la dependencia del servicio de recomendación.
    private final RadioService recomendacionService;


    /**
     * Inicia una "Radio" basada en una canción específica.
     *
     * <p>Retorna un objeto {@code RadioDto} que contiene una lista de canciones
     * recomendadas con base en el género, artista o características de la canción semilla.</p>
     *
     * @param cancionId El identificador único de la canción semilla.
     * @return Un {@code RadioDto} con la lista de recomendaciones.
     * @throws ElementoNoEncontradoException Si la canción semilla no existe.
     */
    @GetMapping("/radio/{cancionId}") // Mapea peticiones HTTP GET a /api/recomendacion/radio/{cancionId}.
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<RadioDto>> iniciarRadio(@PathVariable Long cancionId)
            throws ElementoNoEncontradoException {

        // Llama al servicio para iniciar la radio basada en el ID de la canción.
        RadioDto radioDto = recomendacionService.iniciarRadio(cancionId);

        // Retorna una respuesta 200 OK con el DTO de la radio generada.
        return ResponseEntity.ok(new MensajeDto<>(false, radioDto));
    }



    /**
     * Genera una "Playlist de Descubrimiento Semanal" personalizada para un usuario.
     *
     * <p>Retorna un objeto {@code PlayListDto} que contiene canciones recomendadas
     * basadas en el historial de escucha y los gustos del usuario, buscando nuevos
     * contenidos (descubrimiento).</p>
     *
     * @param idUsuario El identificador único del usuario para el cual generar la playlist.
     * @return Un {@code PlayListDto} con las canciones descubiertas.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    @GetMapping("/descubrimiento/{idUsuario}") // Mapea peticiones HTTP GET a /api/recomendacion/descubrimiento/{idUsuario}.
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<PlayListDto>> generarDescubrimientoSemanal(@PathVariable Long idUsuario)
            throws ElementoNoEncontradoException {

        // Llama al servicio para generar la playlist de descubrimiento para el usuario.
        PlayListDto playlist = recomendacionService.generarDescubrimientoSemanal(idUsuario);

        // Retorna una respuesta 200 OK con el DTO de la playlist generada.
        return ResponseEntity.ok(new MensajeDto<>(false, playlist));
    }

}
