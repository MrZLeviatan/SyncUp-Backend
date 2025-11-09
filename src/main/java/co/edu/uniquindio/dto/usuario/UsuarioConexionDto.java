package co.edu.uniquindio.dto.usuario;

/**
 * Objeto de Transferencia de Datos (DTO) de entrada utilizado para solicitar operaciones de conexión o desconexión social.
 *
 * <p>Este DTO encapsula los IDs de los dos usuarios involucrados en una acción de la red social
 * (ej. "seguir" o "dejar de seguir"), simplificando la recepción de datos en los controladores o servicios.
 *
 * @param idUsuarioPrincipal El ID del usuario que inicia la acción (el usuario que sigue/deja de seguir).
 * @param idUsuarioObjetivo El ID del usuario que es el objetivo de la acción (el usuario que es seguido/dejado de seguir).
 *
 */
public record UsuarioConexionDto(

        Long idUsuarioPrincipal,
        Long idUsuarioObjetivo
) {
}
