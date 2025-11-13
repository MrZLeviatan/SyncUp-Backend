package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.graph.TrieAutocompletado;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.CancionBusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CancionBusquedaServiceImpl implements CancionBusquedaService {


    private final CancionMapper cancionMapper;
    private final CancionRepo cancionRepo;
    private final TrieAutocompletado trie;


    /**
     * Inicializa el Trie con los títulos de las canciones almacenadas en la base de datos.
     * Este método se ejecuta solo una vez para evitar sobrecargar el Trie con inserciones repetidas.
     */
    private void inicializarTrie() {
        if (trie.autocompletar("").isEmpty()) {
            List<Cancion> canciones = cancionRepo.findAll();
            for (Cancion c : canciones) {
                trie.insertar(c.getTitulo());
            }
        }
    }

    /**
     * Devuelve una lista de canciones cuyos títulos coincidan con el prefijo dado.
     * Usa el Trie para encontrar coincidencias de títulos, y luego busca las canciones en la base de datos.
     *
     * @param prefijo Texto parcial introducido por el usuario (por ejemplo "ama").
     * @return Lista de canciones que comienzan con ese prefijo.
     */
    @Override
    public List<CancionDto> autocompletarTitulos(String prefijo) {
        // Aseguramos que el Trie esté inicializado antes de buscar.
        inicializarTrie();

        // Obtenemos las coincidencias de títulos a partir del Trie.
        List<String> coincidencias = trie.autocompletar(prefijo);

        // Si no hay coincidencias, devolvemos una lista vacía.
        if (coincidencias.isEmpty()) {
            return List.of();
        }

        // Consultamos las canciones en la base de datos cuyos títulos estén en la lista del Trie.
        List<Cancion> canciones = cancionRepo.findByTituloInIgnoreCase(coincidencias);

        // Convertimos las entidades a DTO antes de devolverlas.
        return canciones.stream()
                .map(cancionMapper::toDto)
                .collect(Collectors.toList());
    }

}
