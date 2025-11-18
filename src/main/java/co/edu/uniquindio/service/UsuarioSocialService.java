package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.usuario.SugerenciaUsuariosDto;
import co.edu.uniquindio.dto.usuario.UsuarioConexionDto;
import co.edu.uniquindio.dto.usuario.UsuarioDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.utils.estructuraDatos.GrafoSocial;

import java.util.List;

/**
 * Contrato (Interfaz) para el servicio que gestiona las interacciones sociales entre {@link co.edu.uniquindio.models.Usuario}s.
 *
 * <p>Define las operaciones de alto nivel para modificar la estructura del grafo social
 * (conexión y desconexión) y para generar recomendaciones de amistad utilizando la topología del grafo.
 *
 * <p>La implementación concreta de este servicio típicamente interactuará con el
 * {@link GrafoSocial} para modificar y consultar las conexiones.
 *
 * @see UsuarioConexionDto
 * @see SugerenciaUsuariosDto
 */
public interface UsuarioSocialService {


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
