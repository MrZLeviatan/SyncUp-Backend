package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.admin.AdminDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.mapper.AdminMapper;
import co.edu.uniquindio.models.Admin;
import co.edu.uniquindio.repo.AdminRepo;
import co.edu.uniquindio.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementación de la interfaz {@link AdminService}.
 *
 * <p>Contiene la lógica de negocio real para la gestión de administradores,
 * interactuando con el repositorio y el mapeado.</p>
 *
 * @author [Tu Nombre/Nombre del Proyecto]
 * @version 1.0
 * @see AdminService
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    // Inyección de la dependencia del repositorio (capa de persistencia).
    private final AdminRepo adminRepo;
    // Inyección de la dependencia del mapeador (capa de conversión DTO <-> Entidad).
    private final AdminMapper adminMapper;


    /**
     * Obtiene la información de un administrador por su ID y la retorna como DTO.
     *
     * @param idAdmin ID del administrador a buscar.
     * @return DTO con la información del administrador.
     * @throws ElementoNoEncontradoException Si el administrador no se encuentra.
     */
    @Override
    public AdminDto obtenerAdminId(Long idAdmin) throws ElementoNoEncontradoException {
        // Llama al método auxiliar para encontrar la entidad Admin.
        return adminMapper.toDto(buscarAdminId(idAdmin));
    }

    /**
     * Método auxiliar privado para buscar y obtener la entidad {@link Admin} por ID en la base de datos.
     *
     * <p>Utiliza el patrón {@code Optional} para manejar la ausencia del elemento.</p>
     *
     * @param idAdmin ID del administrador a buscar.
     * @return La entidad {@link Admin} encontrada.
     * @throws ElementoNoEncontradoException Si la entidad no existe.
     */
    private Admin buscarAdminId(Long idAdmin) throws ElementoNoEncontradoException {
        return adminRepo.findById(idAdmin)
                .orElseThrow(() -> new ElementoNoEncontradoException("No existe el admin con el id: " + idAdmin));
    }
}
