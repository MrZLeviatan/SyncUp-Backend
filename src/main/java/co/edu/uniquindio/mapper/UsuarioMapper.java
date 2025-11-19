package co.edu.uniquindio.mapper;

import co.edu.uniquindio.dto.usuario.*;
import co.edu.uniquindio.models.Usuario;
import org.mapstruct.*;

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
     * Convierte un DTO {@link RegistrarUsuarioDto} en una entidad {@link Usuario}.
     *<p>
     * - Se ignora el campo ID porque será generado automáticamente por la base de datos. <br>
     * - Las listas se inicializan como {@code LinkedList} para coincidir con la entidad. <br>
     * - Esto evita errores de NullPointer y asegura consistencia con el modelo persistente.
     * </p>
     *
     * @param registrarUsuarioDto DTO con los datos del usuario a registrar.
     * @return Objeto {@link Usuario} listo para ser persistido.
     */
    // Ignora el ID, lo genera la base de datos
    @Mapping(target = "id", ignore = true)
    // Ignoramos la foto de perfil  al ser un archivo inmapeable
    @Mapping(target = "fotoPerfilUrl", ignore = true)
    // Inicializa la lista vacía de canciones favoritas
    @Mapping(target = "cancionesFavoritas", expression = "java(new java.util.LinkedList<>())")
    // Inicializa la lista vacía de artistas favoritos
    @Mapping(target = "artistasGustados", expression = "java(new java.util.LinkedList<>())")
    // Inicializa la lista vacía de usuarios seguidos
    @Mapping(target = "usuariosSeguidos", expression = "java(new java.util.LinkedList<>())")
    Usuario toEntity(RegistrarUsuarioDto registrarUsuarioDto);


    /**
     * Convierte una entidad {@link Usuario} existente a un DTO {@link UsuarioDto}.
     * <p>
     * El mapeo automático de MapStruct a la entidad {@link Usuario} con los atributos (incluyendo canciones, y usuarios seguidos).
     *
     * @param usuario  Entidad {@link Usuario} existente que será mapeado.
     * @return usuarioDto DTO de usuario mapeado.
     */
    UsuarioDto toDto(Usuario usuario);

    /**
     * Actualiza una entidad {@link Usuario} existente con los datos del DTO {@link EditarUsuarioDto}.
     *
     * <p>
     * - Solo modifica los campos explícitos del DTO (por ejemplo, nombre). <br>
     * - No altera las relaciones (listas ni contraseñas). <br>
     * - Se usa {@link MappingTarget} para actualizar una instancia existente.
     * </p>
     *
     * @param editarUsuarioDto DTO con la información actualizada.
     * @param usuario Entidad {@link Usuario} existente que será modificada.
     */
    // Actualiza solo los campos del DTO en la entidad
    @Mapping(target = "id", ignore = true )
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUsuarioFromDto(EditarUsuarioDto editarUsuarioDto, @MappingTarget Usuario usuario);


    /**
     * Actualiza la contraseña de un {@link Usuario} a partir del DTO {@link EditarPasswordDto}.
     *
     * <p>⚠Este método *no realiza validación* ni *encriptación*,
     * solo cambia el valor del campo {@code password} si ya fue validado por la capa de servicio.
     * </p>
     *
     * @param editarPasswordDto DTO con la nueva contraseña.
     * @param usuario Usuario existente que se actualizará.
     */
    // Actualiza solo el campo de contraseña después de validación / Updates only password field after validation.
    @Mapping(target = "id", ignore = true )
    @Mapping(target = "password", source = "nuevoPassword")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePasswordFromDto(EditarPasswordDto editarPasswordDto, @MappingTarget Usuario usuario);


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

}