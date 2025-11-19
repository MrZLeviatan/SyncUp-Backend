package co.edu.uniquindio.mapper;

import co.edu.uniquindio.dto.artista.ArtistaDto;
import co.edu.uniquindio.dto.artista.RegistrarArtistasDto;
import co.edu.uniquindio.models.Artista;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;

/**
 * Interfaz de mapeo (Mapper) para la entidad {@link Artista} y sus DTOs de registro y transferencia.
 *
 * <p>Utiliza la librería MapStruct para generar automáticamente las implementaciones
 * necesarias para convertir datos entre la capa de transferencia (DTO) y la capa de persistencia (Entity).</p>
 *
 */
@Mapper(componentModel = "spring")
public interface ArtistaMapper {

    /**
     * Convierte un {@link RegistrarArtistasDto} a la entidad {@link Artista}.
     *
     * <p>Configura los campos que no están presentes en el DTO para asegurar la integridad de la entidad
     * antes de su persistencia.</p>
     *
     * @param registrarArtistasDto DTO con los datos para el registro de un nuevo artista.
     * @return La entidad {@link Artista} lista para ser guardada.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "canciones", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "urlImagen", ignore = true) // La imagen se maneja aparte
    Artista toEntity(RegistrarArtistasDto registrarArtistasDto);


    // Método por defecto para copiar miembros
    default Set<String> mapMiembros(Set<String> miembros) {
        return miembros == null ? new HashSet<>() : new HashSet<>(miembros);
    }


    /**
     * Convierte la entidad {@link Artista} a su DTO de transferencia básica {@link ArtistaDto}.
     *
     * <p>Utilizado para exponer la información esencial del artista sin incluir colecciones relacionadas.</p>
     *
     * @param artista La entidad Artista de la base de datos.
     * @return El DTO de Artista.
     */
    ArtistaDto toDto (Artista artista);

}
