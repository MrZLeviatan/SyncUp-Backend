package co.edu.uniquindio.mapper;

import co.edu.uniquindio.dto.usuario.SugerenciaUsuariosDto;
import co.edu.uniquindio.models.Usuario;
import org.mapstruct.Mapper;

/**
 * Mapper de transformación para la entidad {@link Usuario} a sus DTO's
 *
 * <p>Utiliza la librería MapStruct para automatizar la conversión. Este mapper es específico
 * para escenarios donde solo se requiere un subconjunto de los datos del usuario (ej. nombre, username).
 *
 * <p>La anotación {@code @Mapper(componentModel = "spring")} registra este mapper como un
 * componente de Spring (Bean), permitiendo su inyección automática.
 *
 * @see Usuario
 * @see SugerenciaUsuariosDto
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {


    /**
     * Convierte la entidad {@link Usuario} en su objeto de transferencia de datos optimizado para sugerencias.
     *
     * <p>El mapeo automático de MapStruct solo incluye los campos que coinciden por nombre entre
     * la entidad {@code Usuario} y el DTO {@code SugerenciaUsuariosDto}.
     *
     * @param usuario La entidad de dominio {@code Usuario} a convertir.
     * @return El DTO {@code SugerenciaUsuariosDto} resultante, con información básica y aligerada del usuario.
     */
    SugerenciaUsuariosDto toDtoSugerenciaUsuarios(Usuario usuario);

    // Métodos de mapeo para otras representaciones DTO del Usuario podrían añadirse aquí.
}