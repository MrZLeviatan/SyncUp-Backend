package co.edu.uniquindio.dto.admin;

/**
 * Record DTO (Data Transfer Object) utilizado para la transferencia de información básica y de autenticación de un Administrador.
 *
 * <p>Dado que es un record, sus campos son inmutables y automáticamente proporciona
 * constructor, métodos de acceso (getters), {@code equals()}, {@code hashCode()}, y {@code toString()}.</p>
 *
 * @param id Identificador único del administrador.
 * @param nombre Nombre completo o social del administrador.
 * @param username Nombre de usuario único utilizado para la autenticación (login).
 * @param password Contraseña del administrador (debe ser manejada de forma segura y hasheada en la capa de servicio/persistencia).
 *
 */
public record AdminDto(

        Long id,
        String nombre,
        String username,
        String password

) {
}
