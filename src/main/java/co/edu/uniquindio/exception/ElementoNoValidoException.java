package co.edu.uniquindio.exception;

/**
 * Excepción personalizada utilizada para indicar que un elemento o valor no cumple con las reglas o criterios de negocio establecidos,
 * incluso si sintácticamente es correcto.
 *
 * <p>Esta excepción es de tipo "Checked Exception" (extiende de {@link Exception}), lo que obliga
 * a los métodos que la lanzan a declarar o manejar explícitamente el incumplimiento de una regla.
 *
 * <p>Casos de uso típicos incluyen:
 * <ul>
 * <li>Un ID de artista (`artistaId`) es provisto en un DTO, pero no corresponde a un artista activo o válido en el sistema.</li>
 * <li>Un valor numérico está fuera de un rango permitido por la lógica de negocio (ej. una duración o rating inválido).</li>
 * </ul>
 */
public class ElementoNoValidoException extends Exception {

    /**
     * Constructor de la excepción que acepta un mensaje descriptivo.
     *
     * <p>El mensaje debe detallar la regla violada, por ejemplo:
     * "El artista con ID 10 no está autorizado para subir contenido."
     *
     * @param message Mensaje que describe la causa específica de la excepción (qué elemento es inválido y por qué).
     */
    public ElementoNoValidoException(String message) {
        super(message);
    }
}
