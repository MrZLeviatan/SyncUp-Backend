package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.artista.ArtistaDto;
import co.edu.uniquindio.dto.artista.RegistrarArtistasDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.graph.TrieAutocompletado;
import co.edu.uniquindio.mapper.ArtistaMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.repo.ArtistaRepo;
import co.edu.uniquindio.service.ArtistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz {@link ArtistaService}.
 *
 * <p>Esta clase contiene la lógica de negocio para la gestión de artistas, incluyendo
 * la persistencia y la búsqueda optimizada mediante Trie.</p>
 *
 * @see ArtistaService
 */
@Service
@RequiredArgsConstructor
public class ArtistaServiceImpl implements ArtistaService {


    private final ArtistaRepo artistaRepo;
    private final ArtistaMapper artistaMapper;
    private final TrieAutocompletado trie;


    /**
     * Método auxiliar para asegurar que el Trie (Árbol de Prefijos) esté cargado con todos los nombres artísticos.
     *
     * <p>Este método se ejecuta condicionalmente solo si el Trie está vacío, garantizando
     * que la carga desde la base de datos solo se haga una vez durante el ciclo de vida de la aplicación.
     * Esto optimiza la función de autocompletado.</p>
     */
    private void inicializarTrie() {
        // Verifica si el Trie está vacío realizando una búsqueda de prefijo vacío.
        if (trie.autocompletar("").isEmpty()) {
            // Si está vacío, se obtienen todos los títulos de la base de datos.
            List<Artista> artistas = artistaRepo.findAll();
            // Se inserta cada nombre artístico
            for (Artista a : artistas) {
                trie.insertar(a.getNombreArtistico());
            }
        }
    }

    /**
     * Registra un nuevo artista en la base de datos.
     *
     * @param registrarArtistasDto DTO con el nombre artístico.
     */
    @Override
    public void agregarArtista(RegistrarArtistasDto registrarArtistasDto) throws ElementoNoEncontradoException {

        // Verificar si el nombre artístico ya existe
        boolean existeNombre = artistaRepo.existsByNombreArtisticoIgnoreCase(registrarArtistasDto.nombreArtistico());

        if (existeNombre) {
            // Si ya existe, lanzar excepción
            throw new ElementoNoEncontradoException("El nombre artístico '"
                    + registrarArtistasDto.nombreArtistico() + "' ya está en uso.");
        }
        // Mapear el DTO a la entidad Artista.
        Artista artista = artistaMapper.toEntity(registrarArtistasDto);

        // Guardar la entidad en la base de datos.
        artistaRepo.save(artista);
    }


    /**
     * Obtiene el DTO de un artista a partir de su ID.
     *
     * @param idArtista ID del artista a buscar.
     * @return DTO con la información del artista.
     * @throws ElementoNoEncontradoException Si no se encuentra el artista.
     */
    @Override
    public ArtistaDto obtenerArtistaId(Long idArtista) throws ElementoNoEncontradoException {
        // Llama al método auxiliar para buscar la entidad y luego la mapea a DTO.
        return artistaMapper.toDto(buscarArtistaId(idArtista));
    }


    /**
     * Implementación de la función de autocompletado de nombres artísticos.
     *
     * @param prefijo Prefijo (cadena parcial) a buscar.
     * @return Lista de {@link ArtistaDto}s coincidentes.
     */
    @Override
    public List<ArtistaDto> autocompletarTitulos(String prefijo) {
        // Aseguramos que el Trie esté inicializado y cargado antes de realizar cualquier búsqueda.
        inicializarTrie();

        // 1. Obtenemos las coincidencias de títulos exactos a partir del Trie (operación rápida en memoria).
        List<String> coincidencias = trie.autocompletar(prefijo);

        // 2. Si no hay coincidencias de nombres, devolvemos una lista de DTOs vacía.
        if (coincidencias.isEmpty()) {
            return List.of();
        }

        // 3. Consultamos los nombres artísticos en la base de datos, buscando aquellas cuyo nombre artístico esté en la lista de coincidencias.
        List<Artista> artistas = artistaRepo.findByNombreArtisticoInIgnoreCase(coincidencias);

        // 4. Convertimos las entidades a DTO antes de devolver el resultado.
        return artistas.stream()
                .map(artistaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Método auxiliar privado para buscar la entidad {@link Artista} por ID.
     *
     * @param idArtista ID del artista.
     * @return La entidad {@link Artista} encontrada.
     * @throws ElementoNoEncontradoException Si el artista no se encuentra en la base de datos.
     */
    private Artista buscarArtistaId(Long idArtista) throws ElementoNoEncontradoException {
        // Utiliza el Optional de findById para manejar el caso de no encontrar el elemento.
        return artistaRepo.findById(idArtista)
                .orElseThrow(()-> new ElementoNoEncontradoException("Artista no encontrado"));
    }


}
