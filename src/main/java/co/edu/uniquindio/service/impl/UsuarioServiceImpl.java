package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.usuario.*;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import co.edu.uniquindio.exception.ElementoRepetidoException;
import co.edu.uniquindio.utils.CloudinaryUtils;
import co.edu.uniquindio.utils.estructuraDatos.GrafoSocial;
import co.edu.uniquindio.mapper.UsuarioMapper;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.AdminRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.UsuarioService;
import co.edu.uniquindio.utils.listasPropias.MiLinkedList;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    private final AdminRepo adminRepo;

    /** Componente para el cifrado y la verificación de contraseñas. */
    private final PasswordEncoder passwordEncoder;

    /** Componente auxiliar para la carga de imagenes */
    private final CloudinaryUtils cloudinaryUtils;

    private final GrafoSocial grafoSocial;


    // Solo para pruebas unitarias o validación interna
    /**
     * Índice en memoria (Cache): Almacena todos los usuarios indexados por su username
     * para búsquedas O(1) y validaciones de unicidad rápidas.
     */
    @Getter
    private final HashMap<String, Usuario> indiceUsuarios = new HashMap<>();


    /**
     * Inicializa la estructura del {@link GrafoSocial} al arrancar la aplicación.
     *
     * <p>El método {@code @PostConstruct} garantiza que el grafo se construya una sola vez con
     * todas las conexiones existentes en la base de datos, optimizando el rendimiento de las
     * subsecuentes búsquedas de sugerencias.
     */
    @PostConstruct
    public void inicializarGrafo() {

        // Obtiene todos los usuarios desde el repositorio (base de datos).
        List<Usuario> usuarios = usuarioRepo.findAllConUsuariosSeguidos();

        // Agrega cada usuario como nodo dentro del grafo.
        usuarios.forEach(grafoSocial::agregarUsuario);

        // Recorre cada usuario para establecer las conexiones existentes.
        for (Usuario u : usuarios) {

            // Por cada usuario seguido, se crea una arista en el grafo.
            for (Usuario seguido : u.getUsuariosSeguidos()) {

                // Conecta los nodos correspondientes en el grafo.
                grafoSocial.conectarUsuarios(u, seguido);
            }
        }
    }


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
            throws ElementoRepetidoException, ElementoNoValidoException {

        // 1. Validar unicidad usando el índice en memoria (O(1)).
        if (indiceUsuarios.containsKey(registrarUsuarioDto.username())) {
            throw new ElementoRepetidoException("El username ya está en uso");
        }

        // 1.2 Validar unicidad usando el repo del Admin.
        if (adminRepo.existsByUsername(registrarUsuarioDto.username())) {
            throw new ElementoRepetidoException("El username ya está en uso");
        }

        // 2, Se sube la imagen de la foto de perfil del usuario.
        String urlImage = cloudinaryUtils.uploadImage(registrarUsuarioDto.fotoPerfil());

        // 3. Convierte el DTO en entidad Usuario usando el mapper
        Usuario usuario = usuarioMapper.toEntity(registrarUsuarioDto);

        // 4. Se codifica el password del usuario.
        usuario.setPassword(passwordEncoder.encode(registrarUsuarioDto.password()));

        // 2.1 Se asigna la url de la foto de perfil
        usuario.setFotoPerfilUrl(urlImage);

        // 5. Guarda el usuario en la base de datos
        Usuario guardado = usuarioRepo.save(usuario);

        // 6. Agrega el usuario al HashMap usando el username como clave
        indiceUsuarios.put(usuario.getUsername(), usuario);

        // 7. Se agrega al grafo social
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

        // 4. Actualiza también el HashMap (Nota: Solo se actualiza el objeto en el HashMap; la clave (username) no cambia.)
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

        // 3.1 Se hace al codificación del nuevo password
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 4. Guarda los cambios en la base de datos
        usuarioRepo.save(usuario);

        // 5. Actualiza también el HashMap (Nota: Solo se actualiza el objeto en el HashMap; la clave (username) no cambia.)
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

        // Eliminar este usuario de las listas de seguidos de los demás
        for (Usuario u : usuarioRepo.findAll()) {
            u.getUsuariosSeguidos().remove(usuario);
        }

        // Limpiar su propia lista de usuarios seguidos
        usuario.getUsuariosSeguidos().clear();

        //  Limpiar la relación de canciones favoritas para evitar violación de FK
        usuario.getCancionesFavoritas().clear();

        // Guardar la entidad limpiada antes de eliminar
        usuarioRepo.save(usuario);

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
    @Transactional
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



    /**
     * Establece una conexión de seguimiento entre dos usuarios.
     *
     * <p>Realiza la actualización tanto en el modelo de dominio (persistencia) como en el grafo en memoria.
     *
     * @param dto Objeto {@link UsuarioConexionDto} con los IDs de los usuarios.
     * @throws ElementoNoEncontradoException Si el usuario principal o el objetivo no existen.
     */
    @Override
    public void seguirUsuario(UsuarioConexionDto dto) throws ElementoNoEncontradoException {

        // Busca el usuario principal por su ID, lanza excepción si no existe.
        Usuario principal = buscarUsuarioId(dto.idUsuarioPrincipal());
        // Busca el usuario objetivo (a seguir) por su ID.
        Usuario objetivo = buscarUsuarioId(dto.idUsuarioObjetivo());

        // Agrega la relación de seguimiento en el modelo de dominio.
        principal.seguirUsuario(objetivo);

        // Guarda los cambios del usuario principal en la base de datos.
        usuarioRepo.save(principal);

        // Actualiza el grafo agregando la conexión.
        grafoSocial.conectarUsuarios(principal, objetivo);
    }


    /**
     * Elimina una conexión de seguimiento entre dos usuarios.
     *
     * <p>Actualiza la persistencia y el grafo para reflejar que un usuario ha dejado de seguir a otro.
     *
     * @param dto Objeto {@link UsuarioConexionDto} con los IDs de los usuarios.
     * @throws ElementoNoEncontradoException Si el usuario principal o el objetivo no existen.
     */
    @Override
    public void dejarDeSeguirUsuario(UsuarioConexionDto dto) throws ElementoNoEncontradoException {
        // Busca el usuario principal por su ID, lanza excepción si no existe.
        Usuario principal = buscarUsuarioId(dto.idUsuarioPrincipal());
        // Busca el usuario objetivo (a seguir) por su ID.
        Usuario objetivo = buscarUsuarioId(dto.idUsuarioObjetivo());

        // Elimina la relación de seguimiento en el modelo de dominio.
        principal.dejarDeSeguirUsuario(objetivo);

        // Persiste los cambios en la base de datos.
        usuarioRepo.save(principal);

        // Actualiza el grafo removiendo la conexión.
        grafoSocial.desconectarUsuarios(principal, objetivo);
    }


    /**
     * Obtiene una lista de sugerencias de usuarios (amigos de amigos).
     *
     * <p>La lógica de búsqueda (BFS a distancia 2) se delega completamente al {@link GrafoSocial},
     * garantizando una alta velocidad de respuesta.
     *
     * @param idUsuario El ID del usuario para el cual se buscan las sugerencias.
     * @return Una {@code List} de {@link SugerenciaUsuariosDto} con los usuarios sugeridos.
     * @throws ElementoNoEncontradoException Si el usuario base no existe.
     */
    @Override
    public List<SugerenciaUsuariosDto> obtenerSugerencias(Long idUsuario) throws ElementoNoEncontradoException {

        // Busca al usuario en la base de datos.
        Usuario usuario = buscarUsuarioId(idUsuario);

        // Obtiene una lista de usuarios sugeridos usando el grafo.
        MiLinkedList<Usuario> sugeridos = grafoSocial.obtenerAmigosDeAmigos(usuario);

        // Convierte cada entidad sugerida en su respectivo DTO.
        return sugeridos.stream()
                .map(usuarioMapper::toDtoSugerenciaUsuarios)
                .collect(Collectors.toList());
    }


    /**
     * Obtiene una lista de todos los usuarios que el usuario con el ID especificado está siguiendo actualmente.
     *
     * <p>El método utiliza el modelo de dominio para acceder a la lista de relaciones sociales del usuario.</p>
     *
     * @param idUsuario El identificador único del usuario principal.
     * @return Una lista de {@link UsuarioDto} con la información de los usuarios seguidos.
     * @throws ElementoNoEncontradoException Si el {@code idUsuario} no corresponde a un usuario existente.
     */
    @Override
    public List<UsuarioDto> obtenerUsuariosSeguidos(Long idUsuario) throws ElementoNoEncontradoException {

        // 1. Busca la entidad Usuario por su ID
        Usuario usuario = buscarUsuarioId(idUsuario);

        // 2. Accede a la lista de usuarios que el usuario principal tiene registrados como 'seguidos'
        List<Usuario> usuariosSeguidos = usuario.getListaUsuariosSeguidos();

        // 3. Mapea la lista de entidades Usuario a una lista de DTOs antes de retornarla.
        return usuariosSeguidos.stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public int cantidadSeguidores(Long idUsuario) throws ElementoNoEncontradoException {
        Usuario usuario = buscarUsuarioId(idUsuario);
        return usuario.contarUsuariosSeguidos();
    }


}
