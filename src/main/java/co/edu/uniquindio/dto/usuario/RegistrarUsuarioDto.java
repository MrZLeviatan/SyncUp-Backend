package co.edu.uniquindio.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

/**
 * Objeto de Transferencia de Datos (DTO) de entrada utilizada para el registro inicial de un nuevo {@link co.edu.uniquindio.models.Usuario}.
 *
 * <p>Utiliza un Record de Java para concisión e inmutabilidad, y define las restricciones básicas
 * de validación de Spring Boot/Jakarta Bean Validation.
 *
 * <p>Este DTO encapsula los datos mínimos necesarios para crear una cuenta en el sistema.
 *
 * @param nombre El nombre completo del usuario. Es obligatorio y no puede ser nulo ni estar vacío
 * (validación {@code @NotBlank}).
 * @param username El nombre de usuario único deseado. Es obligatorio y no puede ser nulo ni estar vacío
 * (validación {@code @NotBlank}).
 * @param password La contraseña elegida por el usuario. Es obligatorio y no puede ser nulo ni estar vacío.
 * Debe ser cifrada por la capa de servicio antes de ser persistida.
 *
 */
public record RegistrarUsuarioDto(

        @NotBlank
        String nombre,
        @NotBlank
        String username,
        @NotBlank
        String password,
        MultipartFile fotoPerfil
) {
}
