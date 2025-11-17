package co.edu.uniquindio.security;

import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.models.Admin;
import co.edu.uniquindio.models.Persona;
import co.edu.uniquindio.models.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * Clase utilitaria para la gestión de JSON Web Tokens (JWT).
 *
 * <p>Esta clase es responsable de generar tokens JWT firmados, de definir la información de
 * autenticación (claims, roles) y de validar la autenticidad y vigencia de los tokens
 * recibidos de los clientes. Es el núcleo del mecanismo de autenticación basado en tokens
 * para el sistema.</p>
 */
@Component
public class JWTUtils {


    // Clave secreta usada para firmar los tokens (mínimo 256 bits para HS256).
    // Es CRUCIAL que esta clave sea larga, compleja y mantenida en secreto.
    private static final String SECRET = "syncup-secret-key-para-firmar-tokens-jwt-de-forma-segura";


    /**
     * Genera un token JWT firmado con una duración de 1 hora.
     *
     * <p>Utiliza la clave secreta definida en la clase para asegurar la autenticidad
     * del token (firma digital).</p>
     *
     * @param id El identificador único del sujeto (usuario o administrador) que será el 'subject' del token.
     * @param claims Un mapa de claims personalizados a incluir en el payload (ej. roles, tipo de usuario).
     * @return El token JWT generado como una cadena de texto (String).
     */
    public String generateToken(String id, Map<String, String> claims) {

        // Se obtiene el instante actual para usar como fecha de emisión y para la expiración
        Instant now = Instant.now(); // Se obtiene el instante actual

        // Se construye el token JWT con los datos proporcionados y firma con una clave secreta
        return Jwts.builder()
                .subject(id) // ID del usuario
                .issuedAt(Date.from(now)) // Fecha de emisión
                .expiration(Date.from(now.plus(1L, ChronoUnit.HOURS))) // Expira en 1 hora
                .claims(claims) // Añade los claims personalizados como el tipo de usuario
                .signWith(getKey(), Jwts.SIG.HS256) // Firma el token con HS256
                .compact(); // Finaliza la construcción y compacta el token en su formato final de cadena.
    }


    /**
     * Genera el mapa de claims estándar y de rol necesarios para el token JWT de login.
     *
     * <p>Este método determina el rol (ROLE_USUARIO o ROLE_ADMIN) basándose en el tipo de
     * instancia de {@code Persona} que se pasa.</p>
     *
     * @param persona El objeto {@code Persona} (Usuario o Admin) que ha iniciado sesión.
     * @return Un mapa de String a String con los claims "username", "nombre" y "rol" para el token.
     * @throws ElementoNoEncontradoException Si la clase de la persona no corresponde a un rol válido o reconocido.
     */
    public Map<String,String> generarTokenLogin(Persona persona)
            throws ElementoNoEncontradoException {

        // Mapa que asocia las clases concretas con su respectivo rol en formato ROLE_*
        Map<Class<?>, String> rolesPorClase = Map.of(
                Usuario.class, "ROLE_USUARIO",
                Admin.class, "ROLE_ADMIN");

        // Obtener el rol correspondiente a la clase específica del objeto persona
        String rol = rolesPorClase.get(persona.getClass());

        // Validar que el rol exista; si no, lanzar excepción indicando que no se encontró el rol
        if (rol == null || rol.isEmpty()) {
            throw new ElementoNoEncontradoException("El tipo de usuario especificado no es válido o no está reconocido");}

        // Retornar un mapa con los datos del token: username, nombre y rol
        return Map.of(
                "username", persona.getUsername(),
                "nombre", persona.getNombre(),
                "rol", rol);
    }


    /**
     * Válida la firma de un token JWT recibido y retorna sus claims seguros (JWS).
     *
     * <p>Si el token es inválido (firma errónea, expirado, malformado), lanza una {@code JwtException}.</p>
     *
     * @param jwtString El token JWT recibido como cadena de texto.
     * @return Un objeto {@code Jws<Claims>} que contiene el header, el payload (claims) y la firma verificada.
     * @throws JwtException Si el token no es válido o está expirado.
     */
    public Jws<Claims> parseJwt(String jwtString) throws JwtException {
        // Crea un parser JWT.
        JwtParser parser = Jwts.parser()
                .verifyWith(getKey()) // Configura el parser para verificar la firma con la clave secreta
                .build();

        // Intenta parsear y validar el token JWT.
        return parser.parseSignedClaims(jwtString); // Devuelve los claims seguros (firmados)
    }

    /**
     * Genera y retorna la clave secreta (SecretKey) requerida por JJWT.
     *
     * <p>Convierte la cadena {@code SECRET} a bytes usando UTF-8 y la utiliza para crear una clave
     * HMAC SHA-256 adecuada para la firma.</p>
     *
     * @return La clave secreta de tipo {@code SecretKey}.
     */
    private SecretKey getKey() {
        // Convierte la cadena SECRET a un array de bytes usando la codificación UTF-8.
        // Crea una clave simétrica para el algoritmo HMAC SHA-256.
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

}
