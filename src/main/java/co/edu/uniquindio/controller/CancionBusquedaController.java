package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.service.CancionBusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/canciones")
@RequiredArgsConstructor
public class CancionBusquedaController {


    private final CancionBusquedaService cancionBusquedaService;


    /**
     * Endpoint para autocompletar títulos de canciones.
     *
     * Ejemplo de petición:
     * GET /api/canciones/autocompletar?prefijo=ama
     *
     * @param prefijo Texto parcial del título a buscar.
     * @return Lista de canciones cuyo título comience con el prefijo.
     */
    @GetMapping("/autocompletar")
    public ResponseEntity<MensajeDto<List<CancionDto>>> autocompletarCanciones(@RequestParam String prefijo) {
        List<CancionDto> resultados = cancionBusquedaService.autocompletarTitulos(prefijo);
        return ResponseEntity.ok().body(new MensajeDto<>(false,resultados));
    }

}
