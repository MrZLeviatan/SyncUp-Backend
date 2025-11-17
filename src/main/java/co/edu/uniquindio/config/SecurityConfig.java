package co.edu.uniquindio.config;

import co.edu.uniquindio.security.AuthenticationEntryPoint;
import co.edu.uniquindio.security.JWTFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Clase de configuración principal para Spring Security.
 *
 * <p>Esta clase establece la política de seguridad web para la aplicación, incluyendo la
 * desactivación de CSRF, la configuración de CORS, la definición de sesiones sin estado (STATELSS)
 * para API REST con JWT, la definición de rutas públicas y protegidas, y la integración del
 * filtro JWT personalizado.</p>
 *
 * @see JWTFilter
 * @see AuthenticationEntryPoint
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize en controladores y servicios
@RequiredArgsConstructor
public class SecurityConfig {

    // Inyección del filtro personalizado para procesar los tokens JWT.
    private final JWTFilter jwtFilter;

    // Inyección del punto de entrada para manejar errores de autenticación.
    private final AuthenticationEntryPoint authenticationEntryPoint;


    /**
     * Configura la cadena de filtros de seguridad HTTP principal de la aplicación.
     *
     * <p>Este método es fundamental para definir el comportamiento de la seguridad en el nivel de solicitud.</p>
     *
     * @param http El objeto {@code HttpSecurity} que permite configurar la seguridad web.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configura la seguridad de la aplicación

        http
                //  Desactiva CSRF (se usa JWT )
                .csrf(AbstractHttpConfigurer::disable)
                //  Habilita CORS con configuración previa
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Define sesiones sin estado (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Reglas de acceso por endpoint y rol
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Docs públicas


                        // Permite el acceso sin autenticación a los endpoints de autenticación (login, registro).
                        .requestMatchers("/api/auth/**").permitAll() // Login/registro públicos
                        .requestMatchers("/api/cancion/filtrar").permitAll()

                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated()
                )
                // Manejo de errores de autenticación
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))

                // Filtro JWT antes de auth por usuario/clave
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Devuelve la cadena de seguridad configurada
        return http.build();
    }



    /**
     * Define la configuración de CORS (Cross-Origin Resource Sharing).
     *
     * <p>Esta configuración permite que navegador web de orígenes específicos realicen solicitudes a la API.</p>
     *
     * @return La fuente de configuración de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        // Objeto que almacena la configuración de CORS.
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes (dominios) permitidos para acceder a la API.
        config.setAllowedOrigins(List.of(
                "http://localhost:4200")); // Ejemplo de frontend en desarrollo local

        // Permitir envío de cookies, tokens y cabeceras de autenticación
        config.setAllowCredentials(true);

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "Accept", "Origin"));

        // Cabeceras expuestas (para que el frontend pueda leerlas)
        config.setExposedHeaders(List.of("Authorization"));

        // Registro de configuración para todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Aplica la configuración a todos los endpoints (/**).
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Define el codificador de contraseñas que será utilizado por Spring Security.
     *
     * <p>Se utiliza {@link BCryptPasswordEncoder} que es un algoritmo de *hashing* seguro
     * y recomendado para contraseñas.</p>
     *
     * @return La instancia del codificador de contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Retorna una instancia de BCryptPasswordEncoder para el hashing de contraseñas.
        return new BCryptPasswordEncoder();
    }


    /**
     * Expone el {@code AuthenticationManager} de Spring Security como un Bean.
     *
     * <p>El {@code AuthenticationManager} es usado internamente por la aplicación (ej. en el
     * controlador de autenticación) para realizar el proceso de login.</p>
     *
     * @param configuration La configuración de autenticación automática de Spring.
     * @return El {@code AuthenticationManager} configurado.
     * @throws Exception Si no se puede obtener el {@code AuthenticationManager}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Devuelve el AuthenticationManager que Spring Security usa internamente
        return configuration.getAuthenticationManager();
    }
}
