package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.admin.AdminDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;

/**
 * Interfaz de servicio que define el contrato para las operaciones de negocio relacionadas con la entidad Administrador.
 *
 * <p>Esta capa es responsable de gestionar las reglas de negocio, la validación y la comunicación
 * con la capa de persistencia para la entidad {@code Admin}.</p>
 *
 */
public interface AdminService {

    /**
     * Obtiene la información de un administrador por su identificador único.
     *
     * <p>Busca la entidad {@code Admin} en la base de datos y la convierte a un {@code AdminDto}
     * para su transferencia.</p>
     *
     * @param idAdmin ID del administrador a buscar.
     * @return {@link AdminDto} con la información del administrador.
     * @throws ElementoNoEncontradoException Si el administrador con el ID dado no existe.
     */
    AdminDto obtenerAdminId(Long idAdmin) throws ElementoNoEncontradoException;

}
