package co.edu.uniquindio.exception;

/**
 * Excepción personalizada utilizada para indicar que se ha intentado registrar o crear un elemento que ya existe en el sistema.
 *
 * <p>Esta excepción es de tipo "Checked Exception" (extiende de {@link Exception}), lo que obliga
 * a los métodos de la capa de servicio a declarar o manejar explícitamente la posibilidad de una
 * violación de la restricción de unicidad.
 *
 * <p>Casos de uso típicos incluyen:
 * <ul>
 * <li>Intentar registrar un {@link co.edu.uniquindio.models.Usuario} con un nombre de usuario (username) o correo electrónico que ya está en uso.</li>
 * <li>Intentar registrar una {@link co.edu.uniquindio.models.Cancion} que ya existe en el catálogo.</li>
 * </ul>
 */
public class ElementoRepetidoException extends Exception {

    /**
     * Constructor de la excepción que acepta un mensaje descriptivo.
     *
     * <p>El mensaje debe especificar el elemento repetido y el valor que causó el conflicto, por ejemplo:
     * "El nombre de usuario 'admin' ya se encuentra registrado."
     *
     * @param message Mensaje que describe la causa específica de la excepción (qué elemento está repetido).
     */
    public ElementoRepetidoException(String message) {
        super(message);
    }
}
