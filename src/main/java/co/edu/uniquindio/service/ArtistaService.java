package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.artista.ArtistaDto;
import co.edu.uniquindio.dto.artista.RegistrarArtistasDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;

import java.util.List;

/**
 * Interfaz de servicio que define el contrato para todas las operaciones de negocio relacionadas con la entidad Artista.
 *
 * <p>Esta capa maneja la lógica de negocio, incluyendo el registro, la consulta básica y
 * la funcionalidad de autocompletado de nombres artísticos.</p>
 *
 */
public interface ArtistaService {

    /**
     * Registra un nuevo artista en el sistema.
     *
     * <p>El proceso convierte el DTO a entidad y lo persiste en la base de datos.</p>
     *
     * @param registrarArtistasDto DTO con el nombre artístico a registrar.
     */
    void agregarArtista(RegistrarArtistasDto registrarArtistasDto) throws ElementoNoEncontradoException;


    /**
     * Obtiene la información básica de un artista por su identificador único.
     *
     * @param idArtista ID del artista a buscar.
     * @return DTO con la información del artista.
     * @throws ElementoNoEncontradoException Si el artista con el ID dado no existe.
     */
    ArtistaDto obtenerArtistaId(Long idArtista) throws ElementoNoEncontradoException;


    /**
     * Proporciona sugerencias de autocompletado para nombres artísticos usando un Árbol de Prefijos (Trie).
     *
     * @param prefijo Prefijo (cadena parcial) a buscar.
     * @return Lista de {@link ArtistaDto}s cuyos nombres artísticos coinciden con el prefijo.
     */
    List<ArtistaDto> autocompletarTitulos(String prefijo);


}
