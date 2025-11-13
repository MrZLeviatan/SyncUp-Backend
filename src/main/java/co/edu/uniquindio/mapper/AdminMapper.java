package co.edu.uniquindio.mapper;

import co.edu.uniquindio.dto.admin.AdminDto;
import co.edu.uniquindio.models.Admin;
import org.mapstruct.Mapper;

/**
 * Interfaz de mapeo (Mapper) para la entidad {@link Admin} y su DTO asociado {@link AdminDto}.
 *
 * <p>Utiliza la librería MapStruct para generar automáticamente las implementaciones
 * en tiempo de compilación, facilitando la conversión de datos entre la capa de persistencia
 * y la capa de transferencia.</p>
 *
 */
@Mapper(componentModel = "spring")
public interface AdminMapper {

    /**
     * Convierte la entidad {@link Admin} (modelo de persistencia) a un {@link AdminDto} (objeto de transferencia).
     *
     * <p>Este método se utiliza generalmente para enviar datos desde el servidor hacia el cliente.</p>
     *
     * @param admin La entidad Administrador de la base de datos.
     * @return El DTO de Administrador.
     */
    AdminDto toDto (Admin admin);

}
