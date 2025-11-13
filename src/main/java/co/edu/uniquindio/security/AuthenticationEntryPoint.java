package co.edu.uniquindio.security;

import co.edu.uniquindio.dto.MensajeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Punto de entrada personalizado para la autenticación dentro del sistema de seguridad.
 * <p>
 * Esta clase se encarga de interceptar los intentos de acceso a recursos protegidos
 * por parte de usuarios no autenticados. Implementa {@link org.springframework.security.web.AuthenticationEntryPoint}
 * para devolver una respuesta JSON clara y consistente cuando ocurre un acceso no autorizado.
 * <p>
 * En lugar de redirigir a una página de inicio de sesión (comportamiento por defecto),
 * este componente responde con un objeto JSON que contiene un mensaje de error y el código
 * HTTP 403 (Forbidden), facilitando su consumo desde aplicaciones frontend (como Angular).
 *
 * <p><strong>Uso:</strong> Registrado automáticamente por Spring Security al ser un componente con la anotación {@code @Component}.
 *
 */
@Component
public class AuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {


    // Método invocado automáticamente por Spring Security cuando un usuario no autenticado intenta acceder a un recurso protegido.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        // Se crea un objeto de respuesta personalizada con un mensaje de error
        MensajeDto<String> dto = new MensajeDto<>(true, "No tienes los permisos necesarios para" +
                " acceder a este recurso");

        // Se configura la respuesta HTTP para que sea de tipo JSON y con estado 403 (Prohibido)
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Se escribe el objeto JSON en la respuesta utilizando Jackson
        response.getWriter().write(new ObjectMapper().writeValueAsString(dto));
        response.getWriter().flush(); // Se asegura de enviar todos los datos
        response.getWriter().close(); // Se cierra el flujo de escritura
    }
}

