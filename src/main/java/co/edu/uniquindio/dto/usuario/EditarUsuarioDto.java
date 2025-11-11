package co.edu.uniquindio.dto.usuario;

import jakarta.validation.constraints.NotNull;

/**
 * Objeto de Transferencia de Datos (DTO) de entrada utilizado para solicitar la actualización de la información de un {@link co.edu.uniquindio.models.Usuario}.
 *
 * <p>Utiliza un Record de Java, lo que lo hace inmutable. Está diseñado para contener
 * el identificador del usuario (obligatorio para saber a quién editar) y los campos que pueden
 * ser modificados.
 *
 * @param id El identificador único del usuario que será editado. Es un campo obligatorio y no puede ser nulo
 * (validación {@code @NotNull}).
 * @param nombre El nuevo nombre completo que se desea asignar al usuario. Este campo es opcional
 * durante la edición, ya que el usuario podría desear actualizar solo otros campos (no incluidos aquí)
 * o dejarlo sin cambios.
 *
 */
public record EditarUsuarioDto(

        @NotNull
        Long id,
        String nombre

) {
}
