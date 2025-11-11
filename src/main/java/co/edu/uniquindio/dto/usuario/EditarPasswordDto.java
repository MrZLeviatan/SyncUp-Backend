package co.edu.uniquindio.dto.usuario;

import jakarta.validation.constraints.NotBlank;

/**
 * Objeto de Transferencia de Datos (DTO) de entrada utilizada para solicitar el cambio de contraseña de un {@link co.edu.uniquindio.models.Usuario}.
 *
 * <p>Este DTO encapsula los tres campos esenciales requeridos para una operación de cambio de contraseña segura:
 * la identidad del usuario y las credenciales antiguas y nuevas.
 *
 * <p>Aunque el campo {@code id} está anotado con {@code @NotBlank} (típicamente usado para {@code String}), se asume
 * que la intención es validar que el ID del usuario se proporcione y no sea nulo, aunque para tipos numéricos
 * se suele preferir {@code @NotNull} o manejar la validación de manera diferente en la capa de servicio.
 *
 * @param id El identificador único del usuario que desea cambiar su contraseña. Es un campo obligatorio.
 * @param passwordAnterior La contraseña actual (anterior) del usuario. Se usa para verificar la identidad
 * antes de permitir el cambio (obligatorio para la seguridad).
 * @param nuevoPassword La nueva contraseña que el usuario desea utilizar. Es un campo obligatorio.
 *
 */
public record EditarPasswordDto(


        @NotBlank
        Long id,
        String passwordAnterior,
        String nuevoPassword

) {
}
