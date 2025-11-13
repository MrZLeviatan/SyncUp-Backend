package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.LoginDto;
import co.edu.uniquindio.dto.TokenDto;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.models.Persona;
import co.edu.uniquindio.repo.AdminRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.security.JWTUtils;
import co.edu.uniquindio.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de autenticación ({@link AuthService}) utilizando JWT.
 *
 * <p>Esta clase se encarga de verificar las credenciales de inicio de sesión de un usuario o
 * administrador y generar un token JWT válido para la autorización.</p>
 *
 * @see AuthService
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Inyección de la utilidad para la generación y manejo de tokens JWT.
    private final JWTUtils jwtUtils;
    // Inyección del codificador de contraseñas para verificar hashes.
    private final PasswordEncoder passwordEncoder;
    // Repositorio para buscar entidades Usuario.
    private final UsuarioRepo usuarioRepo;
    // Repositorio para buscar entidades Admin.
    private final AdminRepo adminRepo;


    /**
     * Procesa el intento de inicio de sesión (login) verificando las credenciales y generando un token JWT.
     *
     * @param loginDto DTO con el nombre de usuario y la contraseña.
     * @return Un {@link TokenDto} que contiene el token JWT generado.
     * @throws ElementoNoEncontradoException Si no se encuentra un usuario o administrador con el username proporcionado.
     * @throws ElementoNoCoincideException Si la contraseña ingresada no coincide con el hash almacenado.
     */
    @Override
    public TokenDto login(LoginDto loginDto) throws ElementoNoEncontradoException, ElementoNoCoincideException {

        String token;

        // Buscar la entidad Persona (Usuario o Admin) por el nombre de usuario.
        Persona persona = buscarPersonaUsername(loginDto.username());

        // Verificar si el password coincide
        if (!passwordEncoder.matches(loginDto.password(), persona.getPassword())) {
            // Lanza excepción si la contraseña no coincide
            throw new ElementoNoCoincideException("La contraseña ingresada es incorrecta");
        }

        // Genera el token de logueo
        // Se utiliza el ID y un mapa de datos generados para incrustar la información de la persona en el payload del JWT.
        token = jwtUtils.generateToken(persona.getId().toString(),jwtUtils.generarTokenLogin(persona));

        // Devolver el token envuelto en el DTO.
        return new TokenDto(token);
    }


    /**
     * Método auxiliar para buscar una entidad {@link Persona} (Usuario o Admin) por su nombre de usuario (username).
     *
     * <p>Realiza una búsqueda secuencial primero en {@code UsuarioRepo} y luego en {@code AdminRepo}.</p>
     *
     * @param username El nombre de usuario a buscar.
     * @return La entidad {@link Persona} encontrada.
     * @throws ElementoNoEncontradoException Si no se encuentra ninguna persona con ese username.
     */
    public Persona buscarPersonaUsername(String username) throws ElementoNoEncontradoException {
        // Buscar primero en el repositorio de Usuario.
        return
                usuarioRepo.findByUsername(username).map(usuario -> (Persona) usuario)
                        // Si no se encuentra un Usuario, buscar en el repositorio de Admin.
                        .or(() -> adminRepo.findByUsername(username).map(admin -> (Persona) admin))
                        // Si el resultado final es vacío, lanzar la excepción.
                        .orElseThrow(() -> new ElementoNoEncontradoException("Persona no encontrado"));
    }
}

