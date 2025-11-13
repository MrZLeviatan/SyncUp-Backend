package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.LoginDto;
import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.TokenDto;
import co.edu.uniquindio.dto.usuario.RegistrarUsuarioDto;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import co.edu.uniquindio.exception.ElementoRepetidoException;
import co.edu.uniquindio.service.AuthService;
import co.edu.uniquindio.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST responsable de manejar los procesos de Autenticación y Registro de usuarios.
 *
 * <p>Expone los *endpoints* públicos necesarios para que los usuarios puedan registrarse
 * y obtener un token de acceso (login) para futuras interacciones con la API.</p>
 *
 * @see AuthService
 * @see UsuarioService
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Inyección del servicio para la gestión de usuarios (Registro).
    private final UsuarioService usuarioService;

    // Inyección del servicio para la autenticación (Login).
    private final AuthService authService;


    /**
     * Endpoint para el registro de un nuevo usuario en el sistema.
     *
     * <p>Es un endpoint público que no requiere autenticación previa.</p>
     *
     * @param registrarUsuarioDto DTO que contiene los datos del nuevo usuario a registrar. Se valida mediante {@code @Valid}.
     * @return {@code ResponseEntity} con un mensaje de éxito.
     * @throws ElementoRepetidoException Si el nombre de usuario o algún otro campo único ya existe.
     */
    @PostMapping("/registro-usuario")
    public ResponseEntity<MensajeDto<String>> registrarUsuarios(@Valid @RequestBody RegistrarUsuarioDto registrarUsuarioDto)
            throws ElementoRepetidoException {
        // Llama al servicio para realizar la lógica de negocio y persistencia del registro.
        usuarioService.registroUsuario(registrarUsuarioDto);
        // Retorna una respuesta 200 OK con un mensaje de éxito.
        return ResponseEntity.ok().body(new MensajeDto<>(false,"Registro logrado exitosamente."));
    }


    /**
     * Endpoint para el inicio de sesión (Login).
     *
     * <p>Verifica las credenciales y, si son correctas, genera y devuelve un Token JWT.</p>
     *
     * @param loginDto DTO que contiene el nombre de usuario y la contraseña.
     * @return {@code ResponseEntity} con el {@link TokenDto} generado.
     * @throws ElementoNoValidoException Si los datos de entrada son inválidos.
     * @throws ElementoNoCoincideException Si la contraseña no coincide.
     * @throws ElementoNoEncontradoException Si el nombre de usuario no existe en el sistema.
     */
    @PostMapping("/login")
    public ResponseEntity<MensajeDto<TokenDto>> login (@RequestBody LoginDto loginDto)
            throws ElementoNoValidoException, ElementoNoCoincideException, ElementoNoEncontradoException {
        // Llama al servicio de autenticación para verificar credenciales y generar el token.
        TokenDto tokenDto = authService.login(loginDto);
        // Retorna una respuesta 200 OK con el TokenDto.
        return ResponseEntity.ok().body(new MensajeDto<>(false, tokenDto));
    }

}
