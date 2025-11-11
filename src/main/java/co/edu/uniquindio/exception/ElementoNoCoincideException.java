package co.edu.uniquindio.exception;

/**
 * Excepción personalizada utilizada para indicar que dos elementos, entidades o valores que debían ser
 * idénticos (o coincidir) han fallado la validación de igualdad.
 *
 * <p>Esta excepción es de tipo "Checked Exception" (extiende de {@link Exception}), lo que obliga
 * a los métodos que la lanzan a declararla o manejarla explícitamente, promoviendo un manejo
 * riguroso de fallos lógicos en el flujo de negocios.
 *
 * <p>Casos de uso típicos incluyen:
 * <ul>
 * <li>Verificación de la **contraseña anterior** en un proceso de cambio de clave.</li>
 * <li>Validación de que un usuario autenticado coincide con el propietario de un recurso que intenta modificar.</li>
 * <li>Confirmación de campos repetidos (ej. la confirmación de la nueva contraseña).</li>
 * </ul>
 */
public class ElementoNoCoincideException extends Exception {

    /**
     * Constructor de la excepción que acepta un mensaje descriptivo.
     *
     * @param message Mensaje que describe la causa específica de la excepción (qué elementos no coinciden).
     */
    public ElementoNoCoincideException(String message) {
        super(message);
    }
}
