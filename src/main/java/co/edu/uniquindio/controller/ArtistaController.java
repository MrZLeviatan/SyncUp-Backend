package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.artista.ArtistaDto;
import co.edu.uniquindio.dto.artista.RegistrarArtistasDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.service.ArtistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que gestiona todas las operaciones relacionadas con la entidad {@link Artista}.
 *
 * <p>Expone los *endpoints* para registrar artistas, obtener información por ID y realizar
 * la función de autocompletado de nombres artísticos, interactuando con la capa de servicio
 * de negocio ({@link ArtistaService}).</p>
 *
 * @see ArtistaService
 */
    @RestController
    @RequestMapping("/api/artistas")
    @RequiredArgsConstructor
    public class ArtistaController {
    
        // Inyección de la dependencia del servicio de artistas.
        private final ArtistaService artistaService;
    
        /**
         * Endpoint para registrar un nuevo artista en el sistema.
         *
         * @param registrarArtistasDto DTO con el nombre artístico a registrar.
         * @return {@code ResponseEntity} con un mensaje de éxito.
         * @throws ElementoNoEncontradoException Si el registro fallara por alguna dependencia (aunque aquí no aplica directamente, se mantiene por la firma).
         */
        @PostMapping("/registrar")
        @PreAuthorize("hasRole('ADMIN')") // Restringe el acceso solo a usuarios con el rol 'ADMIN'.
        public ResponseEntity<MensajeDto<String>> registrarArtista(@RequestBody RegistrarArtistasDto registrarArtistasDto) throws ElementoNoEncontradoException {
            // Llama al servicio para realizar la lógica de negocio y persistencia.
            artistaService.agregarArtista(registrarArtistasDto);
            // Retorna una respuesta 200 OK con un mensaje de éxito.
            return ResponseEntity.ok().body(new MensajeDto<>(false,"Registro logrado exitosamente."));
        }
    
    
        /**
         * Endpoint para obtener la información básica de un artista por su ID.
         *
         * @param idArtista ID único del artista, obtenido de la variable de ruta (PathVariable).
         * @return {@code ResponseEntity} con el DTO del artista.
         * @throws ElementoNoEncontradoException Si el artista con el ID dado no existe.
         */
        @GetMapping("/{idArtista}")
        @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
        public ResponseEntity<MensajeDto<ArtistaDto>> obtenerArtistaPorId(@PathVariable Long idArtista) throws ElementoNoEncontradoException {
            // Llama al servicio para buscar el artista por ID.
            ArtistaDto artistaDto = artistaService.obtenerArtistaId(idArtista);
    
            // Retorna una respuesta 200 OK con el DTO del usuario encontrado.
            return ResponseEntity.ok().body(new MensajeDto<>(false, artistaDto));
        }
    
    
        /**
         * Endpoint para la funcionalidad de autocompletado de nombres artísticos.
         *
         * <p>Busca en la estructura Trie interna del servicio todos los nombres artísticos que
         * comienzan con el {@code prefijo} dado.</p>
         *
         * @param prefijo Texto parcial del nombre artístico a buscar, obtenido como parámetro de consulta (@RequestParam).
         * @return {@code ResponseEntity} con una lista de {@code ArtistaDto}s coincidentes.
         */
        @GetMapping("/autocompletar")
        @PreAuthorize("hasAnyRole('USUARIO','ADMIN')") // Permite el acceso a USUARIO y ADMIN.
        public ResponseEntity<MensajeDto<List<ArtistaDto>>> autocompletarArtistas(@RequestParam String prefijo) {
            // Llama al servicio para ejecutar la búsqueda por prefijo (síncrona y rápida).
            List<ArtistaDto> resultados = artistaService.autocompletarTitulos(prefijo);
            // Retorna la respuesta 200 OK con los resultados envueltos en MensajeDto.
            return ResponseEntity.ok().body(new MensajeDto<>(false,resultados));
        }
    }
