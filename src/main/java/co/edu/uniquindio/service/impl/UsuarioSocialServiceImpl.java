package co.edu.uniquindio.service.impl;


import co.edu.uniquindio.dto.usuario.SugerenciaUsuariosDto;
import co.edu.uniquindio.dto.usuario.UsuarioConexionDto;
import co.edu.uniquindio.exception.ElemenoNoEncontradoException;
import co.edu.uniquindio.graph.GrafoSocial;
import co.edu.uniquindio.mapper.UsuarioMapper;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.UsuarioSocialService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación concreta de la interfaz {@link UsuarioSocialService} para la gestión de la red social.
 *
 * <p>Este servicio utiliza el patrón de *Dual-Write* o *Actualización Dual*, donde toda acción de conexión
 * se registra en dos lugares:
 * <ul>
 * <li>Persistencia: En la base de datos a través de {@link UsuarioRepo} para garantizar la durabilidad.</li>
 * <li>Memoria: En él {@link GrafoSocial} para permitir consultas rápidas de sugerencias (BFS).</li>
 * </ul>
 *
 * @see UsuarioSocialService
 * @see GrafoSocial
 */
@Service
@RequiredArgsConstructor
public class UsuarioSocialServiceImpl implements UsuarioSocialService {

    /** Repositorio para la persistencia y recuperación de entidades {@link Usuario}. */
    private final UsuarioRepo usuarioRepo;

    /** Mapper para la conversión de {@link Usuario} a {@link SugerenciaUsuariosDto}. */
    private final UsuarioMapper usuarioMapper;

    /** Estructura en memoria que modela las relaciones sociales para un acceso rápido. */
    private final GrafoSocial grafoSocial = new GrafoSocial();


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
        List<Usuario> usuarios = usuarioRepo.findAll();

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
     * Establece una conexión de seguimiento entre dos usuarios.
     *
     * <p>Realiza la actualización tanto en el modelo de dominio (persistencia) como en el grafo en memoria.
     *
     * @param dto Objeto {@link UsuarioConexionDto} con los IDs de los usuarios.
     * @throws ElemenoNoEncontradoException Si el usuario principal o el objetivo no existen.
     */
    @Override
    public void seguirUsuario(UsuarioConexionDto dto) throws ElemenoNoEncontradoException {

        // Busca el usuario principal por su ID, lanza excepción si no existe.
        Usuario principal = obtenerUsuarioPorId(dto.idUsuarioPrincipal());
        // Busca el usuario objetivo (a seguir) por su ID.
        Usuario objetivo = obtenerUsuarioPorId(dto.idUsuarioObjetivo());

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
     * @throws ElemenoNoEncontradoException Si el usuario principal o el objetivo no existen.
     */
    @Override
    public void dejarDeSeguirUsuario(UsuarioConexionDto dto) throws ElemenoNoEncontradoException {
        // Busca el usuario principal por su ID, lanza excepción si no existe.
        Usuario principal = obtenerUsuarioPorId(dto.idUsuarioPrincipal());
        // Busca el usuario objetivo (a seguir) por su ID.
        Usuario objetivo = obtenerUsuarioPorId(dto.idUsuarioObjetivo());

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
     * @throws ElemenoNoEncontradoException Si el usuario base no existe.
     */
    @Override
    public List<SugerenciaUsuariosDto> obtenerSugerencias(Long idUsuario) throws ElemenoNoEncontradoException {

        // Busca al usuario en la base de datos.
        Usuario usuario = obtenerUsuarioPorId(idUsuario);

        // Obtiene una lista de usuarios sugeridos usando el grafo.
        List<Usuario> sugeridos = grafoSocial.obtenerAmigosDeAmigos(usuario);

        // Convierte cada entidad sugerida en su respectivo DTO.
        return sugeridos.stream()
                .map(usuarioMapper::toDtoSugerenciaUsuarios)
                .collect(Collectors.toList());
    }


    /**
     * Método auxiliar para buscar un usuario por ID o lanzar una excepción de negocio.
     *
     * @param idUsuario El ID del usuario a buscar.
     * @return El objeto {@link Usuario} si es encontrado.
     * @throws ElemenoNoEncontradoException Si la entidad no se encuentra en la base de datos.
     */
    private Usuario obtenerUsuarioPorId(Long idUsuario) throws ElemenoNoEncontradoException {
        // Busca al usuario mediante el ID en la base de datos.
        return  usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new ElemenoNoEncontradoException("Usuario no encontrado"));
    }
}
