package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.usuario.EditarPasswordDto;
import co.edu.uniquindio.dto.usuario.EditarUsuarioDto;
import co.edu.uniquindio.dto.usuario.RegistrarUsuarioDto;
import co.edu.uniquindio.dto.usuario.UsuarioDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoRepetidoException;
import co.edu.uniquindio.graph.GrafoSocial;
import co.edu.uniquindio.mapper.UsuarioMapper;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.UsuarioService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Implementación del servicio de gestión del ciclo de vida del {@link Usuario} ({@link UsuarioService}).
 *
 * <p>Esta clase aplica una estrategia de *caché de lectura rápida* al indexar todos los usuarios
 * en un {@code HashMap<String, Usuario>} en memoria. Esto permite realizar búsquedas por
 * {@code username} y validaciones de unicidad en tiempo constante (O(1)), evitando consultas
 * repetitivas a la base de datos para esas operaciones.
 *
 * <p>Es la capa responsable de la seguridad de las contraseñas, utilizando {@link PasswordEncoder}.
 *
 * @see UsuarioService
 * @see PasswordEncoder
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    /** Mapper de MapStruct para la conversión entre DTOs y la entidad {@link Usuario}. */
    private final UsuarioMapper usuarioMapper;

    /** Repositorio de Spring Data JPA para la persistencia del usuario. */
    private final UsuarioRepo usuarioRepo;

    /** Componente para el cifrado y la verificación de contraseñas. */
    private final PasswordEncoder passwordEncoder;

    private final GrafoSocial grafoSocial;

    // Solo para pruebas unitarias o validación interna
    /**
     * Índice en memoria (Cache): Almacena todos los usuarios indexados por su username
     * para búsquedas O(1) y validaciones de unicidad rápidas.
     */
    @Getter
    private final HashMap<String, Usuario> indiceUsuarios = new HashMap<>();


    /**
     * Carga inicial del caché de usuarios al iniciar la aplicación.
     *
     * <p>Utiliza {@code @PostConstruct} para asegurar que el {@code indiceUsuarios} se puebla
     * inmediatamente después de que el servicio sea inicializado por Spring.
     */
    @PostConstruct
    public void inicializarCache() {
        List<Usuario> usuarios = usuarioRepo.findAll();
        usuarios.forEach(usuario -> indiceUsuarios.put(usuario.getUsername(), usuario));
    }


    /**
     * Registra un nuevo usuario, validando la unicidad del username y cifrando la contraseña.
     *
     * @param registrarUsuarioDto DTO de entrada con los datos del nuevo usuario.
     * @throws ElementoRepetidoException Si el username ya existe en el índice.
     */
    @Override
    public void registroUsuario(RegistrarUsuarioDto registrarUsuarioDto)
            throws ElementoRepetidoException {

        // 1. Validar unicidad usando el índice en memoria (O(1)).
        if (indiceUsuarios.containsKey(registrarUsuarioDto.username())) {
            throw new ElementoRepetidoException("El username ya está en uso");
        }

        // 2. Convierte el DTO en entidad Usuario usando el mapper
        Usuario usuario = usuarioMapper.toEntity(registrarUsuarioDto);

        // 3. Se codifica el password del usuario.
        usuario.setPassword(passwordEncoder.encode(registrarUsuarioDto.password()));

        // 4. Guarda el usuario en la base de datos
        Usuario guardado = usuarioRepo.save(usuario);

        // 5. Agrega el usuario al HashMap usando el username como clave
        indiceUsuarios.put(usuario.getUsername(), usuario);

        // 6. Se agrega al grafo social
        grafoSocial.agregarUsuario(guardado);
    }


    /**
     * Actualiza la información no sensible de un usuario.
     *
     * <p>El método asume la existencia de {@code usuarioMapper.updateUsuarioFromDto(dto, usuario)}
     * para realizar la copia de propiedades de actualización.
     *
     * @param editarUsuarioDto DTO con el ID del usuario y los campos a modificar.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     */
    @Override
    public void editarUsuario(EditarUsuarioDto editarUsuarioDto) throws ElementoNoEncontradoException {

        // 1. Se busca al usuario mediante su ID.
        Usuario usuario = buscarUsuarioId(editarUsuarioDto.id());

        // 2. Se hace el mapeo de actualización del usuario
        usuarioMapper.updateUsuarioFromDto(editarUsuarioDto, usuario);

        // 3. Guarda los cambios en la base de datos
        usuarioRepo.save(usuario);

        // 4. Actualiza también el HashMap ( Nota: Solo se actualiza el objeto en el HashMap; la clave (username) no cambia.)
        indiceUsuarios.put(usuario.getUsername(), usuario);
    }


    /**
     * Cambia la contraseña de un usuario tras verificar la credencial anterior.
     *
     * @param editarPasswordDto DTO con el ID, password anterior y nuevo password.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     * @throws ElementoNoCoincideException Si la contraseña anterior es incorrecta.
     */
    @Override
    public void editarPassword(EditarPasswordDto editarPasswordDto)
            throws ElementoNoEncontradoException, ElementoNoCoincideException {

        // 1. Se busca al usuario mediante su ID.
        Usuario usuario = buscarUsuarioId(editarPasswordDto.id());

        // 2. Verificamos si las credenciales coinciden.
        if (!passwordEncoder.matches(editarPasswordDto.passwordAnterior(), usuario.getPassword())) {
            // Lanza excepción si la contraseña no coincide
            throw new ElementoNoCoincideException("La contraseña ingresada es incorrecta");}

        // 3. Se hace el mapeo para la actualización del password del usuario
        usuarioMapper.updatePasswordFromDto(editarPasswordDto, usuario);

        // 4. Guarda los cambios en la base de datos
        usuarioRepo.save(usuario);

        // 5. Actualiza también el HashMap ( Nota: Solo se actualiza el objeto en el HashMap; la clave (username) no cambia.)
        indiceUsuarios.put(usuario.getUsername(), usuario);
    }


    /**
     * Elimina permanentemente un usuario de la DB y del caché.
     *
     * @param idUsuario ID del usuario a eliminar.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     */
    @Override
    public void eliminarUsuario(Long idUsuario) throws ElementoNoEncontradoException {

        // 1. Se busca al usuario mediante su ID.
        Usuario usuario = buscarUsuarioId(idUsuario);

        // 1. Eliminar de la lista de usuarios seguidos de otros
        for (Usuario u : grafoSocial.obtenerEstructura().keySet()) {
            u.getUsuariosSeguidos().remove(usuario); // Eliminar referencia en listas
            grafoSocial.desconectarUsuarios(u, usuario); // Actualizar grafo en memoria
        }


        // 2. Eliminar todas sus conexiones en el grafo
        grafoSocial.obtenerEstructura().remove(usuario);

        // 3. Se elimina el usuario de la base de datos
        usuarioRepo.delete(usuario);

        // 4. Se elimina el usuario del HashMap
        indiceUsuarios.remove(usuario.getUsername());
    }


    /**
     * Busca un usuario por ID en la base de datos y retorna su representación DTO.
     *
     * @param idUsuario ID del usuario a buscar.
     * @return {@link UsuarioDto} del usuario encontrado.
     * @throws ElementoNoEncontradoException Si el usuario no existe.
     */
    @Override
    public UsuarioDto obtenerUsuarioId(Long idUsuario) throws ElementoNoEncontradoException {
        // Se mapea el usuario al objeto de transferencia DTO
        return usuarioMapper.toDto(buscarUsuarioId(idUsuario));
    }


    /**
     * Busca un usuario por su username utilizando el índice en memoria (O(1)).
     *
     * @param username Username del usuario a buscar.
     * @return {@link UsuarioDto} del usuario encontrado.
     * @throws ElementoNoEncontradoException Si el usuario no existe en el índice.
     */
    @Override
    public UsuarioDto obtenerUsuarioUsername(String username) throws ElementoNoEncontradoException {

        // 1. Se busca al usuario en el HashMap usando el username como clave
        Usuario usuario = indiceUsuarios.get(username);

        // 2. Si no encuentra el usuario, lanza una exception
        if (usuario == null) {
            throw new ElementoNoEncontradoException("Usuario no encontrado por su username: " + username);
        }
        //  3. Se convierte la entidad Usuario en un DTO antes de retornarlo
        return usuarioMapper.toDto(usuario);
    }


    /**
     * Obtiene una lista de todos los usuarios registrados, utilizando el caché en memoria.
     *
     * @return Lista de todos los usuarios en formato {@link UsuarioDto}.
     */
    @Override
    public List<UsuarioDto> obtenerUsuarios() {

        // 1.. Se obtiene la colección de valores del HashMap (los objetos Usuario)
        Collection<Usuario> usuarios = indiceUsuarios.values();

        // 2. Se transforman las entidades Usuario a DTO usando el mapper
        return usuarios.stream()
                .map(usuarioMapper::toDto)
                .toList(); // Retorna la lista final de usuarios en formato DTO
    }

    /**
     * Método auxiliar para buscar la entidad {@link Usuario} por ID en el repositorio.
     *
     * @param idUsuario ID del usuario a buscar.
     * @return Entidad {@link Usuario} encontrada.
     * @throws ElementoNoEncontradoException Si el usuario no existe en la base de datos.
     */
    private Usuario buscarUsuarioId(Long idUsuario) throws ElementoNoEncontradoException {
        return usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ElementoNoEncontradoException("Usuario no encontrado por su Id"));
    }
}
