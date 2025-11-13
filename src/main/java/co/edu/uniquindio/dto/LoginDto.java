package co.edu.uniquindio.dto;

/**
 * Record DTO (Data Transfer Object) utilizado para la transferencia de credenciales de inicio de sesión.
 *
 * <p>Encapsula el nombre de usuario y la contraseña proporcionados por el cliente
 * en el proceso de autenticación. Al ser un record, garantiza la inmutabilidad de los datos.</p>
 *
 * @param username El nombre de usuario (o correo electrónico) para la autenticación.
 * @param password La contraseña proporcionada por el usuario.
 *
 */
public record LoginDto(

        String username,
        String password
) {
}
