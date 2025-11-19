package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.usuario.*;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoNoValidoException;
import co.edu.uniquindio.exception.ElementoRepetidoException;
import co.edu.uniquindio.utils.estructuraDatos.GrafoSocial;

import java.util.List;

/**
 * Interfaz para el servicio que gestiona las operaciones del ciclo de vida del {@link co.edu.uniquindio.models.Usuario}.
 *
 * <p>Define las operaciones CRUD básicas y las tareas relacionadas con la administración de la cuenta,
 * utilizando DTOs de entrada para garantizar la seguridad y la correcta validación de los datos.
 *
 * <p>La implementación concreta de este servicio es responsable de aplicar la lógica de negocio,
 * la validación de seguridad (ej. cifrado de contraseñas) y la persistencia de los cambios.
 *
 * @see RegistrarUsuarioDto
 * @see EditarUsuarioDto
 * @see EditarPasswordDto
 */
public interface UsuarioService {

    /**
     * Procesa la solicitud para registrar un nuevo usuario en el sistema.
     *
     * <p>La implementación debe validar la unicidad del username, cifrar la contraseña y persistir
     * la nueva entidad.
     *
     * @param registrarUsuarioDto DTO de entrada con los datos del nuevo usuario.
     * @throws ElementoRepetidoException Si el username ya existe en el sistema.
     */
    void registroUsuario(RegistrarUsuarioDto registrarUsuarioDto)
            throws ElementoRepetidoException, ElementoNoValidoException;


    /**
     * Procesa la solicitud para actualizar la información básica de un usuario existente.
     *
     * @param editarUsuarioDto DTO de entrada con el ID del usuario y los campos a modificar.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    void editarUsuario(EditarUsuarioDto editarUsuarioDto)
            throws ElementoNoEncontradoException;

    /**
     * Procesa la solicitud para cambiar la contraseña de un usuario.
     *
     * <p>La implementación debe verificar la contraseña anterior antes de actualizar y persistir la nueva.
     *
     * @param editarPasswordDto DTO de entrada con el ID, la contraseña anterior y la nueva contraseña.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     * @throws ElementoNoCoincideException Si la contraseña anterior proporcionada es incorrecta.
     */
    void editarPassword(EditarPasswordDto editarPasswordDto)
            throws ElementoNoEncontradoException, ElementoNoCoincideException;

    /**
     * Elimina permanentemente un usuario del sistema por su identificador único.
     *
     * @param idUsuario El ID del usuario a eliminar.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    void eliminarUsuario(Long idUsuario) throws ElementoNoEncontradoException;


    /**
     * Obtiene la información completa de un usuario por su identificador único.
     *
     * @param idUsuario El ID del usuario a buscar.
     * @return Un {@link UsuarioDto} con la información del usuario.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    UsuarioDto obtenerUsuarioId(Long idUsuario)
            throws ElementoNoEncontradoException;

    /**
     * Obtiene la información completa de un usuario por su nombre de usuario (username).
     *
     * @param username El nombre de usuario (username) a buscar.
     * @return Un {@link UsuarioDto} con la información del usuario.
     * @throws ElementoNoEncontradoException Si el username no existe.
     */
    UsuarioDto obtenerUsuarioUsername(String username) throws ElementoNoEncontradoException;


    /**
     * Obtiene una lista de todos los usuarios registrados en el sistema.
     *
     * @return Una {@code List} de {@link UsuarioDto} conteniendo todos los usuarios.
     */
    List<UsuarioDto> obtenerUsuarios();

    /**
     * Establece una conexión social (seguimiento o amistad) entre los dos usuarios especificados en el DTO.
     *
     * <p>Esta operación debe reflejarse en la estructura del {@link GrafoSocial}.
     *
     * @param dto Objeto {@link UsuarioConexionDto} que contiene los IDs del usuario principal y del usuario objetivo.
     * @throws ElementoNoEncontradoException Si alguno de los IDs de usuario no existe.
     */
    void seguirUsuario(UsuarioConexionDto dto) throws ElementoNoEncontradoException;


    /**
     * Elimina la conexión social (dejar de seguir o eliminar amistad) entre los dos usuarios especificados en el DTO.
     *
     * <p>Esta operación debe revertir la conexión en él {@link GrafoSocial}.
     *
     * @param dto Objeto {@link UsuarioConexionDto} que contiene los IDs del usuario principal y del usuario objetivo.
     * @throws ElementoNoEncontradoException Si alguno de los IDs de usuario no existe.
     */
    void dejarDeSeguirUsuario(UsuarioConexionDto dto) throws ElementoNoEncontradoException;


    /**
     * Obtiene una lista de usuarios sugeridos para seguir, basados en el principio de "amigos de amigos".
     *
     * <p>La implementación utiliza el algoritmo BFS sobre el {@link GrafoSocial}
     * para encontrar usuarios que están a una distancia de dos aristas.
     *
     * @param idUsuario El ID del usuario para el cual se están buscando las sugerencias.
     * @return Una {@code List} de {@link SugerenciaUsuariosDto} con la información aligerada de los usuarios sugeridos.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    List<SugerenciaUsuariosDto> obtenerSugerencias(Long idUsuario) throws ElementoNoEncontradoException;

    /**
     * Obtiene una lista de usuarios seguidos por el usuario seleccionado.
     *
     * @param idUsuario El ID del usuario para el cual se están buscando los usuarios seguidos
     * @return Una {@code List} de {@link UsuarioDto} con la información de los usuarios seguidos.
     * @throws ElementoNoEncontradoException Si el ID del usuario no existe.
     */
    List<UsuarioDto> obtenerUsuariosSeguidos(Long idUsuario) throws ElementoNoEncontradoException;
}
