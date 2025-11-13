package co.edu.uniquindio.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) utilizado para encapsular y transferir el token de autenticación (JWT).
 *
 * <p>Esta clase se utiliza como cuerpo de respuesta en los endpoints de inicio de sesión
 * para devolver el token que el cliente usará en futuras peticiones seguras.</p>
 *
 * <p>Las anotaciones {@code @Getter} y {@code @Setter} de Lombok generan automáticamente
 * los métodos de acceso para los campos.</p>
 *
 */
@Getter
@Setter
public class TokenDto {

    /**
     * Almacena el valor del token de autentificación (JWT).
     *
     * <p>Esta cadena de texto es crucial para la seguridad, ya que valida la identidad del usuario.</p>
     */
    private String token;

    /**
     * Constructor que inicializa el DTO con el token.
     *
     * @param token El token JWT generado después de la autenticación exitosa.
     */
    public TokenDto(String token) {
        // Asigna el token recibido como parámetro al campo 'token' de la clase.
        this.token = token;
    }
}
