package co.edu.uniquindio.controller;

import co.edu.uniquindio.dto.MensajeDto;
import co.edu.uniquindio.dto.admin.AdminDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que gestiona las operaciones de consulta relacionadas con la entidad Administrador.
 *
 * <p>Expone endpoints para obtener información de los administradores del sistema,
 * interactuando con la capa de servicio de negocio ({@link AdminService}).</p>
 *
 * @see AdminService
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;


    /**
     * Endpoint para obtener la información básica de un admin por su ID.
     *
     * @param idAmin ID único del admin, obtenido de la variable de ruta (PathVariable).
     * @return {@code ResponseEntity} con el DTO del artista.
     * @throws ElementoNoEncontradoException Si el artista con el ID dado no existe.
     */
    @GetMapping("/{idAdmin}")
    @PreAuthorize("hasRole('ADMIN')") // Restringe el acceso solo a usuarios con el rol 'ADMIN'.
    public ResponseEntity<MensajeDto<AdminDto>> obtenerAdminPorId(@PathVariable Long idAdmin) throws ElementoNoEncontradoException {
        // Llama al servicio para buscar el admin por ID.
        AdminDto adminDto = adminService.obtenerAdminId(idAdmin);

        // Retorna una respuesta 200 OK con el DTO del usuario encontrado.
        return ResponseEntity.ok().body(new MensajeDto<>(false, adminDto));
    }
}
