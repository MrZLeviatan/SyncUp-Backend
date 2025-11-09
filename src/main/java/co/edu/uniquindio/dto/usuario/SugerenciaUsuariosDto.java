package co.edu.uniquindio.dto.usuario;

/**
 * Objeto de Transferencia de Datos (DTO) diseñado para representar información pública y aligerada de un usuario.
 *
 * <p>Este DTO se utiliza típicamente en contextos de recomendación o listados donde solo se necesita
 * la identidad básica del usuario (como en una lista de "usuarios sugeridos para seguir").
 * Evita exponer información sensible o innecesaria.
 *
 * @param id El identificador único de la entidad {@link co.edu.uniquindio.models.Usuario}.
 * @param nombre El nombre completo del usuario.
 * @param username El nombre de usuario único.
 *
 * @see co.edu.uniquindio.mapper.UsuarioMapper
 */
public record SugerenciaUsuariosDto(

        Long id,
        String nombre,
        String username
) {
}
