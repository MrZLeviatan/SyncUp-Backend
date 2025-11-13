package co.edu.uniquindio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de seguridad que intercepta cada solicitud HTTP para validar un token JWT.
 *
 * <p>Extiende de {@link OncePerRequestFilter} para garantizar que el filtro se ejecute
 * exactamente una vez por solicitud, sin importar los *dispatchers* internos.</p>
 *
 * <p>Si el token es válido y está presente, se extrae la información del usuario ({@code subject} y {@code rol})
 * y se registra en el contexto de seguridad de Spring Security, permitiendo así
 * el acceso a recursos protegidos bajo autorización.</p>
 *
 * <p><strong>Encabezado esperado:</strong> {@code Authorization: Bearer [token]}</p>
 *
 * @see JWTUtils Utilidad encargada de validar y decodificar el JWT.
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    // Inyección de la utilidad para el manejo de JWT.
    private final JWTUtils jwtUtil;


    /**
     * Lógica principal del filtro, ejecutada una vez por solicitud.
     *
     * <p>Este método decide si una solicitud debe ser procesada por el contexto de seguridad
     * (validación de token) o si debe continuar libremente (rutas públicas).</p>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Obtiene la URI de la solicitud.
        String requestURI = request.getRequestURI();

        // Se verifica si la URI coincide con rutas que deben ser accesibles sin autenticación.
        if (requestURI.equals("/") ||
                requestURI.startsWith("/error") || // Rutas de error internas de Spring.
                requestURI.startsWith("/api/auth/")) // Endpoints de autenticación (login, registro).
            {
                // Si es una ruta pública, continúa con la cadena de filtros sin validar el token.
            chain.doFilter(request, response);
            return;
        }


        // Solo rutas protegidas: obtener token del header Authorization
        // Intenta extraer el token JWT del encabezado HTTP.
        String token = getToken(request);

        // Validar que el token exista
        if (token == null || token.isEmpty()) {
            // Si no hay token, registra el error y retorna un 401 (Unauthorized).
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token missing or empty");
            return;
        }

        try {
            // 1. Validar token JWT y obtener payload (Claims).
            // La utilidad JWTUtils verifica la firma, expiración y sintaxis.
            Jws<Claims> payload = jwtUtil.parseJwt(token);

            // Extrae el subject (ID/Username) del usuario.
            String username = payload.getPayload().getSubject(); // Nombre del usuario

            // Extrae el claim 'rol' del usuario (el rol asignado en JWTUtils).
            String role = payload.getPayload().get("rol", String.class); // Rol del usuario

            // 2. Normalizar rol al formato de Spring Security (ROLE_XXX).
            // Maneja casos donde el rol ya viene con "ROL_" o sin ningún prefijo.
            if (role.startsWith("ROL_")) {
                // Si viene como ROL_USUARIO, lo convierte a ROLE_USUARIO.
                role = "ROLE_" + role.substring(4);
            } else if (!role.startsWith("ROLE_")) {
                // Si viene sin prefijo (ej. USUARIO), lo convierte a ROLE_USUARIO.
                role = "ROLE_" + role;
            }

            // 3. Autenticación en Spring Security Context.
            // Verifica que el username exista y que no haya una autenticación activa en el contexto.
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Crea un objeto UserDetails requerido por Spring Security.
                UserDetails userDetails = new User(
                        username,
                        "", // No se requiere contraseña aquí
                        // Crea una autoridad de Spring Security con el rol normalizado.
                        List.of(new SimpleGrantedAuthority(role))
                );

                // Crea el objeto de autenticación que será almacenado en el contexto.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, // El principal (identidad) es el UserDetails.
                        null, // Las credenciales son nulas (ya validadas por el token).
                        userDetails.getAuthorities() // Las autoridades (roles) del usuario.
                );

                // Establece el objeto de autenticación en el contexto de seguridad global.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Si la validación del JWT falla (expirado, firma inválida, etc.).
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        // Continuar con la cadena de filtros de Spring Security hacia el controlador.
        chain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT de la solicitud HTTP.
     *
     * <p>Busca el encabezado {@code Authorization} y remueve el prefijo {@code Bearer }.</p>
     *
     * @param req La solicitud HTTP.
     * @return La cadena del token JWT (sin el prefijo "Bearer") o {@code null} si no se encuentra.
     */
    private String getToken(HttpServletRequest req) {
        // Obtiene el valor del encabezado Authorization.
        String header = req.getHeader("Authorization");
        // Verifica si el encabezado no es nulo y comienza con "Bearer".
        return header != null && header.startsWith("Bearer ")
                // Si es válido, remueve "Bearer" y retorna el token.
                ? header.replace("Bearer ", "")
                // Si no, retorna null.
                : null;
    }
}
