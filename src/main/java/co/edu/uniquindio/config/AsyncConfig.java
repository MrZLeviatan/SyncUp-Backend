package co.edu.uniquindio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase de configuración de Spring Boot para habilitar el procesamiento asíncrono (asynchronous execution).
 *
 * <p>La anotación {@code @EnableAsync} instruye a Spring para que busque la anotación {@code @Async}
 * en los métodos de los *beans* administrados. Cuando se invoca un método anotado con {@code @Async},
 * este se ejecuta en un *thread* separado, permitiendo que el *thread* principal (generalmente el de la petición web)
 * continúe su ejecución sin esperar la finalización de la tarea. Esto es crucial para mejorar
 * el rendimiento y la capacidad de respuesta de la aplicación en tareas que consumen mucho tiempo (ej. envío de emails, procesamiento de archivos).</p>
 *
 * @see EnableAsync
 */
@Configuration
@EnableAsync    // Habilita la capacidad de ejecutar métodos en hilos separados (asíncronamente).
public class AsyncConfig {

    // Esta clase no requiere lógica interna; su propósito es únicamente habilitar la funcionalidad asíncrona
    // a través de las anotaciones de Spring.
}
