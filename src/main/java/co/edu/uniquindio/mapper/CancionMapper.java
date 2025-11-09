package co.edu.uniquindio.mapper;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.models.Cancion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de transformación para la entidad {@link Cancion} y su DTO asociado ({@link CancionDto}).
 *
 * <p>Utiliza la librería MapStruct para generar automáticamente el código de mapeo.
 *
 * <p>La anotación {@code @Mapper(componentModel = "spring")} registra este mapper como un
 * componente de Spring (Bean), permitiendo su inyección automática en los servicios.
 *
 * @see Cancion
 * @see CancionDto
 * @see org.mapstruct.Mapper
 */
@Mapper(componentModel = "spring")
public interface CancionMapper {

    /**
     * Convierte una entidad {@link Cancion} en su objeto de transferencia de datos (DTO) {@link CancionDto}.
     *
     * <p>El mapeo realiza una transformación específica para optimizar el rendimiento y reducir el tamaño
     * del DTO en la comunicación:
     * <ul>
     * <li>Todos los campos con el mismo nombre se mapean automáticamente.</li>
     * <li>Transformación de Relaciones: El objeto {@code artistaPrincipal} se reemplaza por su ID.</li>
     * </ul>
     *
     * @param cancion La entidad de dominio {@code Cancion} a mapear.
     * @return El DTO {@code CancionDto} resultante, listo para ser enviado.
     */
    @Mapping(target = "idArtista", source = "artistaPrincipal.id")
    CancionDto toDto(Cancion cancion);  // Se mapea la entidad Canción a CancionDto

}
