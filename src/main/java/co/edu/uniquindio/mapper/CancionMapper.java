package co.edu.uniquindio.mapper;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.dto.cancion.EditarCancionDto;
import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.dto.usuario.EditarUsuarioDto;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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


    /**
     * Convierte el DTO de registro {@link RegistrarCancionDto} en una nueva entidad {@link Cancion}.
     *
     * <p>Esta es una operación de mapeo de escritura (DTO de entrada a entidad).
     *
     * <p>Se ignoran varios campos de la entidad que no vienen directamente del DTO,
     * ya que estos serán establecidos posteriormente por la lógica del servicio o
     * por el gestor de persistencia. Los campos ignorados son:
     * <ul>
     * <li>`id`: Es generado por la base de datos (clave primaria).</li>
     * <li>`urlCancion`, `urlPortada`: Son generadas por el servicio de almacenamiento (ej. Cloud Storage)
     * después de procesar los archivos {@code MultipartFile} del DTO.</li>
     * <li>`artistaPrincipal`: Debe ser buscado en la base de datos usando el `artistaId` del DTO
     * y asignado por el servicio.</li>
     * <li>`duracion`: Debe ser calculada por el servicio analizando el archivo de audio.</li>
     * </ul>
     *
     * @param registrarCancionDto DTO de entrada con los metadatos y archivos de la canción.
     * @return Una nueva entidad {@code Cancion} con los datos básicos del DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "urlCancion", ignore = true)
    @Mapping(target = "urlPortada", ignore = true)
    @Mapping(target = "artistaPrincipal", ignore = true)
    @Mapping(target = "duracion", ignore = true)
    Cancion toEntity(RegistrarCancionDto registrarCancionDto);


    /**
     * Actualiza una entidad {@link Cancion} existente con los datos proporcionados en el DTO de edición.
     *
     * <p>Esta operación es un *update* (actualización de entidad) de MapStruct. Se utiliza
     * la anotación {@code @MappingTarget} para indicar que la entidad {@code cancion} es el
     * destino de la actualización, conservando su identidad y relaciones no mapeadas.</p>
     *
     * <ul>
     * <li>Ignorar ID: El mapeo ignora explícitamente el campo {@code id} del DTO de entrada
     * para asegurar que el ID de la entidad de destino se mantenga inalterado, preservando
     * la identidad de la canción.</li>
     * <li>Actualización Parcial: Solo los campos presentes y no nulos en el {@code EditarCancionDto}
     * actualizarán los valores correspondientes en la entidad {@code cancion}.</li>
     * </ul>
     *
     * @param editarCancionDto DTO con los nuevos metadatos de la canción.
     * @param cancion La entidad {@code Cancion} existente que será actualizada (destino del mapeo).
     */
    @Mapping(target = "id", ignore = true)
    void updateCancionFromDto(EditarCancionDto editarCancionDto, @MappingTarget Cancion cancion);



}


