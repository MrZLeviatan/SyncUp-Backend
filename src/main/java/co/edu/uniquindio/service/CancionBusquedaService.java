package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.cancion.CancionDto;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz de servicio que define el contrato para las operaciones de búsqueda y consulta avanzada de canciones.
 *
 * <p>Esta capa utiliza estructuras de datos especializadas (ej. Trie) y capacidades de concurrencia
 * de Spring para ofrecer respuestas rápidas a consultas complejas y búsquedas de autocompletado.</p>
 *
 */
public interface CancionBusquedaService {


    /**
     * Proporciona sugerencias de autocompletado para títulos de canciones utilizando un Árbol de Prefijos (Trie).
     *
     * <p>Esta función es síncrona y rápida, ya que opera sobre una estructura de datos en memoria.</p>
     *
     * @param prefijo La cadena de texto (parcial) que el usuario ha ingresado.
     * @return Una lista de {@link CancionDto}s con los títulos completos que coinciden con el prefijo dado.
     */
    List<CancionDto> autocompletarTitulos(String prefijo);



    /**
     * Busca canciones aplicando diversos filtros y ejecuta la operación de manera asíncrona.
     *
     * <ul>
     * <li>`@Async`: Indica que este método debe ejecutarse en un *hilo de ejecución separado*
     * (fuera del hilo principal de la petición web). Esto evita bloquear la respuesta HTTP
     * mientras la base de datos realiza la consulta, mejorando la escalabilidad del sistema.</li>
     *
     * <li>`CompletableFuture<List<CancionDto>>`: Es un contenedor que representa un *resultado que estará disponible en el futuro*.
     * Dado que el método se ejecuta en segundo plano (`@Async`), no puede devolver la lista de inmediato.
     * En su lugar, devuelve este `CompletableFuture`, que eventualmente contendrá la `List<CancionDto>`
     * cuando la consulta a la base de datos finalice. El controlador o el código que llama a este servicio
     * puede continuar su ejecución y luego "esperar" o reaccionar a la finalización de esta operación futura.</li>
     * </ul>
     *
     * @param artista Nombre del artista para filtrar (opcional).
     * @param genero Género musical para filtrar (opcional).
     * @param anioLanzamiento Año de lanzamiento para filtrar (opcional).
     * @param pagina El número de página de resultados a retornar (para paginación).
     * @param size El tamaño de la página (cantidad de resultados por página).
     * @return Un {@code CompletableFuture} que contendrá la lista de canciones filtradas.
     */
    @Async
    CompletableFuture<List<CancionDto>> listarCancionesFiltro(
            String artista, String genero, Integer anioLanzamiento, int pagina, int size);

}
