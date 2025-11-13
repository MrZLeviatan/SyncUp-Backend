package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.service.CancionBusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * Controlador REST para gestionar las funcionalidades de búsqueda y filtrado de canciones.
 *
 * <p>Expone endpoints para la búsqueda rápida por prefijo (autocompletado) y para
 * consultas complejas con filtros, utilizando ejecución asíncrona para mejorar
 * el rendimiento del servidor en tareas que requieren más tiempo.</p>
 *
 * @see CancionBusquedaService
 */
@RestController
@RequestMapping("/api/canciones")
@RequiredArgsConstructor
public class CancionBusquedaController {

    // Inyección de la dependencia del servicio de búsqueda de canciones.
    private final CancionBusquedaService cancionBusquedaService;


    /**
     * Endpoint para obtener sugerencias de autocompletado de títulos de canciones.
     *
     * <p>Utiliza una estructura Trie en la capa de servicio para ofrecer una respuesta muy rápida.</p>
     *
     * @param prefijo Texto parcial del título a buscar, obtenido como parámetro de consulta.
     * @return Una respuesta HTTP 200 OK con una lista de {@code CancionDto}s coincidentes.
     */
    @GetMapping("/autocompletar")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public ResponseEntity<MensajeDto<List<CancionDto>>> autocompletarCanciones(@RequestParam String prefijo) {
        // Llama al servicio para ejecutar la búsqueda por prefijo (síncrona y rápida).
        List<CancionDto> resultados = cancionBusquedaService.autocompletarTitulos(prefijo);
        // Retorna la respuesta 200 OK con los resultados envueltos en MensajeDto.
        return ResponseEntity.ok().body(new MensajeDto<>(false,resultados));
    }


    /**
     * Endpoint para listar y filtrar canciones mediante criterios y paginación, ejecutándose de forma asíncrona.
     *
     * <p>Retorna un {@code CompletableFuture}, delegando a Spring la gestión del hilo de ejecución
     * y permitiendo que la respuesta se complete cuando el resultado del servicio esté disponible.</p>
     *
     * @param artista Filtro opcional por nombre del artista.
     * @param genero Filtro opcional por género musical.
     * @param anioLanzamiento Filtro opcional por año de lanzamiento.
     * @param pagina El número de página solicitado (por defecto 0).
     * @param size El tamaño de la página (por defecto 10).
     * @return Un {@code CompletableFuture} que se resolverá en un {@code ResponseEntity<List<CancionDto>>}.
     */
    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
    public CompletableFuture<ResponseEntity<List<CancionDto>>> listarCancionesFiltro(
            @RequestParam(required = false) String artista, // Parámetro opcional.
            @RequestParam(required = false) String genero, // Parámetro opcional.
            @RequestParam(required = false) Integer anioLanzamiento, // Parámetro opcional.
            @RequestParam(defaultValue = "0") int pagina, // Parámetro con valor por defecto.
            @RequestParam(defaultValue = "10") int size) { // Parámetro con valor por defecto.

        // Llama al servicio, que retorna un CompletableFuture (la tarea se ejecuta en segundo plano).
        return cancionBusquedaService.listarCancionesFiltro(artista, genero, anioLanzamiento, pagina, size)
                // Cuando el CompletableFuture del servicio se completa con la lista (List<CancionDto>),
                // la función thenApply la envuelve en un ResponseEntity 200 OK.
                .thenApply(ResponseEntity::ok);
    }

}
