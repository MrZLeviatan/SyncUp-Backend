package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.LoginDto;
import co.edu.uniquindio.dto.TokenDto;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;

/**
 * Interfaz de servicio que define el contrato para la funcionalidad de autenticación (Auth).
 *
 * <p>Esta capa es responsable de verificar las credenciales de inicio de sesión de cualquier tipo de usuario (administrador o usuario regular)
 * y generar un token de acceso seguro para la autorización de futuras peticiones.</p>
 *
 */
public interface AuthService {

    /**
     * Procesa el intento de inicio de sesión (login) mediante la verificación de credenciales.
     *
     * <p>Si las credenciales son válidas, genera un token de seguridad JWT que se le otorga al cliente.</p>
     *
     * @param loginDto DTO que contiene el nombre de usuario y la contraseña.
     * @return Un {@link TokenDto} que encapsula el token JWT generado para la sesión.
     * @throws ElementoNoEncontradoException Si el nombre de usuario no corresponde a ninguna persona registrada.
     * @throws ElementoNoCoincideException Si la contraseña proporcionada es incorrecta.
     */
    TokenDto login(LoginDto loginDto)
            throws ElementoNoEncontradoException, ElementoNoCoincideException;
}
