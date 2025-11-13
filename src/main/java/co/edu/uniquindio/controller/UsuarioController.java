package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.usuario.EditarPasswordDto;
import co.edu.uniquindio.dto.usuario.EditarUsuarioDto;
import co.edu.uniquindio.dto.usuario.UsuarioDto;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 *
 * <p>Expone los *endpoints* para las operaciones CRUD y de autenticación relacionadas con
 * la entidad {@code Usuario}. Todos los métodos en este controlador están protegidos y requieren
 * un token JWT válido.</p>
 */
@RestController
@RequestMapping("/api/usuario") // Prefijo base para todos los endpoints de este controlador.
@RequiredArgsConstructor
public class UsuarioController {

    // Inyección de la dependencia del servicio de usuario.
    private final UsuarioService usuarioService;


    /**
     * Actualiza la información básica de un usuario existente.
     *
     * @param editarUsuarioDto DTO con los datos del usuario a modificar.
     * @return Mensaje de éxito o error envuelto en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    @PutMapping("/editar") // Mapea peticiones HTTP PUT a /api/usuario/editar.
    @PreAuthorize("hasRole('USUARIO')") // Solo usuarios con rol USUARIO
    public ResponseEntity<MensajeDto<String>> editarUsuario(@RequestBody EditarUsuarioDto editarUsuarioDto) throws ElementoNoEncontradoException {
        // Llama al servicio para realizar la lógica de negocio de edición.
        usuarioService.editarUsuario(editarUsuarioDto);
        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false, "Usuario actualizado correctamente"));
    }


    /**
     * Cambia la contraseña de un usuario verificando la contraseña anterior.
     *
     * @param editarPasswordDto DTO con el ID, contraseña anterior y nueva.
     * @return Mensaje de confirmación o error envuelto en {@code ResponseEntity}.
     * @throws ElementoNoCoincideException Si la contraseña anterior es incorrecta.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    @PutMapping("/editar-password") // Mapea peticiones HTTP PUT a /api/usuario/editar-password.
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<MensajeDto<String>> editarPassword(@RequestBody EditarPasswordDto editarPasswordDto)
            throws ElementoNoCoincideException, ElementoNoEncontradoException {

        // Llama al servicio para realizar la lógica de cambio de contraseña.
        usuarioService.editarPassword(editarPasswordDto);

        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false, "Usuario actualizado correctamente"));
    }


    /**
     * Elimina un usuario por su ID.
     *
     * @param idUsuario ID del usuario a eliminar, obtenido de la URL.
     * @return Mensaje de confirmación o error envuelto en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    @DeleteMapping("/eliminar/{idUsuario}") // Mapea peticiones HTTP DELETE a /api/usuario/eliminar/{id}.
    @PreAuthorize("hasRole('ADMIN')") // Restringe el acceso solo a usuarios con el rol 'ADMIN'.
    public ResponseEntity<MensajeDto<String>> eliminarUsuario(@PathVariable Long idUsuario) throws ElementoNoEncontradoException {

        // Llama al servicio para realizar la lógica de eliminación.
        usuarioService.eliminarUsuario(idUsuario);

        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false, "Usuario eliminado correctamente"));
    }


    /**
     * Obtiene un usuario por su ID.
     *
     * @param idUsuario ID del usuario a buscar, obtenido de la URL.
     * @return {@code UsuarioDto} con la información encontrada, envuelto en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    @GetMapping("/{idUsuario}") // Mapea peticiones HTTP GET a /api/usuario/{id}.
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public ResponseEntity<MensajeDto<UsuarioDto>> obtenerUsuarioPorId(@PathVariable Long idUsuario) throws ElementoNoEncontradoException {

        // Llama al servicio para buscar el usuario por ID.
        UsuarioDto usuarioDto = usuarioService.obtenerUsuarioId(idUsuario);

        // Retorna una respuesta 200 OK con el DTO del usuario encontrado.
        return ResponseEntity.ok().body(new MensajeDto<>(false, usuarioDto));
    }


    /**
     * Obtiene un usuario por su username.
     *
     * @param username Nombre de usuario a buscar, obtenido de la URL.
     * @return {@code UsuarioDto} con la información encontrada, envuelto en {@code ResponseEntity}.
     * @throws ElementoNoEncontradoException Si el username no existe.
     */
    @GetMapping("/username/{username}") // Mapea peticiones HTTP GET a /api/usuario/username/{username}.
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public ResponseEntity<MensajeDto<UsuarioDto>> obtenerUsuarioPorUsername(@PathVariable String username) throws ElementoNoEncontradoException {

        // Llama al servicio para buscar el usuario por username.
        UsuarioDto usuarioDto = usuarioService.obtenerUsuarioUsername(username);

        // Retorna una respuesta 200 OK con el DTO del usuario encontrado.
        return ResponseEntity.ok().body(new MensajeDto<>(false, usuarioDto));

    }


    /**
     * Obtiene una lista de todos los usuarios registrados en el sistema.
     *
     * @return Lista de {@code UsuarioDto} de todos los usuarios, envuelta en {@code ResponseEntity}.
     */
    @GetMapping("/listar") // Mapea peticiones HTTP GET a /api/usuario/listar.
    @PreAuthorize("hasRole('ADMIN')") // Restringe el acceso solo a usuarios con el rol 'ADMIN'.
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> listarUsuarios() {

        // Llama al servicio para obtener la lista completa de usuarios.
        List<UsuarioDto> usuarios = usuarioService.obtenerUsuarios();

        // Retorna una respuesta 200 OK con la lista de DTOs.
        return ResponseEntity.ok().body(new MensajeDto<>(false, usuarios));
    }
}
