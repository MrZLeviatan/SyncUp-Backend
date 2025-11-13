package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.usuario.SugerenciaUsuariosDto;
import co.edu.uniquindio.dto.usuario.UsuarioConexionDto;
import co.edu.uniquindio.dto.usuario.UsuarioDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.service.UsuarioSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar interacciones sociales entre usuarios.
 *
 * <p>Expone los *endpoints* necesarios para que los usuarios puedan seguir a otros,
 * dejar de seguir y recibir sugerencias de usuarios a seguir, basándose en la lógica de negocio.</p>
 *
 * @see UsuarioSocialService
 */
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class UsuarioSocialController {

    // Inyección de la dependencia del servicio de lógica social.
    private final UsuarioSocialService usuarioSocialService;


    /**
     * Establece una conexión social, permitiendo que un usuario siga a otro.
     *
     * @param dto DTO con los IDs del usuario principal (el que sigue) y del usuario a seguir.
     * @return Mensaje de confirmación envuelto en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si alguno de los IDs de usuario no existe.
     */
    @PostMapping("/seguir") // Mapea peticiones HTTP POST a /api/social/seguir.
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<String>> seguirUsuario(@RequestBody UsuarioConexionDto dto)
            throws ElementoNoEncontradoException {

        // Llama al servicio para ejecutar la lógica de "seguir".
        usuarioSocialService.seguirUsuario(dto);

        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok(new MensajeDto<>(false, "Usuario seguido correctamente"));
    }


    /**
     * Elimina la conexión social, permitiendo que un usuario deje de seguir a otro.
     *
     * @param dto DTO con los IDs del usuario principal (el que deja de seguir) y del usuario afectado.
     * @return Mensaje de confirmación envuelto en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si alguno de los IDs de usuario no existe.
     */
    @PostMapping("/dejar-de-seguir") // Mapea peticiones HTTP POST a /api/social/dejar-de-seguir.
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<String>> dejarDeSeguirUsuario(@RequestBody UsuarioConexionDto dto)
            throws ElementoNoEncontradoException {

        // Llama al servicio para ejecutar la lógica de "dejar de seguir".
        usuarioSocialService.dejarDeSeguirUsuario(dto);

        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok(new MensajeDto<>(false, "Usuario dejado de seguir correctamente"));
    }


    /**
     * Obtiene sugerencias de usuarios para seguir, a menudo basadas en amigos en común (amigos de amigos).
     *
     * @param idUsuario ID del usuario para el cual se deben generar las sugerencias, obtenido de la URL.
     * @return Lista de {@code SugerenciaUsuariosDto} con usuarios sugeridos, envuelta en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si el ID del usuario principal no existe.
     */
    @GetMapping("/sugerencias/{idUsuario}") // Mapea peticiones HTTP GET a /api/social/sugerencias/{idUsuario}.
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<List<SugerenciaUsuariosDto>>> obtenerSugerencias(@PathVariable Long idUsuario)
            throws ElementoNoEncontradoException {

        // Llama al servicio para obtener la lista de sugerencias.
        List<SugerenciaUsuariosDto> sugerencias = usuarioSocialService.obtenerSugerencias(idUsuario);

        // Retorna una respuesta 200 OK con la lista de sugerencias.
        return ResponseEntity.ok(new MensajeDto<>(false, sugerencias));
    }


    /**
     * Obtiene la lista de usuarios seguidos por el usuario especificado.
     *
     * <p>Expone un endpoint GET para que el usuario autenticado consulte las relaciones sociales
     * que ha establecido (a quién está siguiendo).</p>
     *
     * @param idUsuario ID del usuario que realiza la consulta, obtenido de la URL.
     * @return Lista de {@link UsuarioDto} con la información de los usuarios seguidos, envuelta en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    @GetMapping("/seguidos/{idUsuario}") // Mapea peticiones HTTP GET a /api/social/seguidos/{idUsuario}.
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosSeguidos(@PathVariable Long idUsuario)
            throws ElementoNoEncontradoException {

        // 1. Llama al servicio para obtener la lista de DTOs de los usuarios seguidos.
        List<UsuarioDto> usuariosSeguidos = usuarioSocialService.obtenerUsuariosSeguidos(idUsuario);

        // 2. Retorna una respuesta 200 OK con la lista de usuarios seguidos.
        return ResponseEntity.ok(new MensajeDto<>(false, usuariosSeguidos));
    }


}
