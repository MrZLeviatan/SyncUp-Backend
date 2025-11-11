package co.edu.uniquindio.dto.usuario;

import co.edu.uniquindio.dto.cancion.CancionDto;

import java.util.List;

/**
 * Objeto de Transferencia de Datos (DTO) que representa la información completa de un {@link co.edu.uniquindio.models.Usuario}.
 *
 * <p>Utiliza un Record de Java para garantizar la inmutabilidad. Este DTO está diseñado para ser usado
 * en contextos donde se requiere el perfil completo del usuario, incluyendo sus relaciones sociales
 * y su contenido personalizado (favoritos).
 *
 * <p>**Advertencia de Seguridad:** Exponer el campo {@code password} cifrado en un DTO completo para el
 * frontend es una práctica de seguridad cuestionable. Generalmente, este campo se omite o se maneja
 * en un DTO interno.
 *
 * @param id El identificador único del usuario.
 * @param nombre El nombre completo del usuario.
 * @param username El nombre de usuario único.
 * @param password La contraseña (asumida como cifrada) del usuario.
 * @param cancionesFavoritas La lista de {@link CancionDto} que el usuario ha marcado como favoritas.
 * @param usuariosSeguidos La lista de otros {@code UsuarioDto} que este usuario está siguiendo, lo que
 * demuestra una relación recursiva dentro del DTO.
 *
 * @see CancionDto
 */
public record UsuarioDto(

        Long id,
        String nombre,
        String username,
        String password,
        List<CancionDto> cancionesFavoritas,
        List<UsuarioDto> usuariosSeguidos

) {
}
