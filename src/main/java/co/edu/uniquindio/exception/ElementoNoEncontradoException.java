package co.edu.uniquindio.exception;

/**
 * Excepción personalizada utilizada para indicar que un elemento o entidad solicitada no fue encontrado.
 *
 * <p>Esta excepción es de tipo "Checked Exception" (extiende de {@link Exception}), lo que obliga
 * a los métodos que la lanzan a declararla en su cláusula {@code throws} o a manejarla
 * explícitamente, asegurando que el desarrollador gestione la lógica de negocio para entidades faltantes.
 *
 * <p>Generalmente, se lanza en la capa de servicios o repositorios cuando se espera recuperar
 * un objeto (como un {@link co.edu.uniquindio.models.Cancion} o {@link co.edu.uniquindio.models.Usuario})
 * por su ID o por un criterio único, y este no existe.
 *
 */
public class ElementoNoEncontradoException extends Exception {

    /**
     * Constructor de la excepción que acepta un mensaje descriptivo.
     *
     * <p>El mensaje debe especificar el elemento que se intentó encontrar y el criterio de búsqueda,
     * por ejemplo: "Canción con ID 123 no encontrada".
     *
     * @param message Mensaje que describe la causa específica de la excepción (qué elemento no fue encontrado).
     */
    public ElementoNoEncontradoException(String message) {
        super(message);
    }
}