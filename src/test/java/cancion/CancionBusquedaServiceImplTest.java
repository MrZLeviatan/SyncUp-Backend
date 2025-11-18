package cancion;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.graph.TrieAutocompletado;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.impl.CancionBusquedaServiceImpl;
import co.edu.uniquindio.utils.collections.MiLinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CancionBusquedaServiceImpl}.
 * (ES) Pruebas unitarias usando mocks del Trie, Repo y Mapper.
 */
class CancionBusquedaServiceImplTest {

    @Mock
    private CancionRepo cancionRepo;

    @Mock
    private CancionMapper cancionMapper;

    @Mock
    private TrieAutocompletado trie;

    @InjectMocks
    private CancionBusquedaServiceImpl cancionBusquedaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * (EN) Creates a mocked MiLinkedList<T> with a fake iterator.
     * (ES) Crea un MiLinkedList<T> simulado con un iterador falso.
     */
    /**
     * Creates a real MiLinkedList<T> with given values.
     * (ES) Crea una MiLinkedList real con valores.
     */
    private MiLinkedList<String> crearMiLinkedList(String... valores) {
        MiLinkedList<String> lista = new MiLinkedList<>();
        for (String v : valores) {
            lista.add(v);
        }
        return lista;
    }

    
    @Test
    void testAutocompletarTitulos_SinCoincidencias() {
        String prefijo = "XYZ";

        // Trie not empty
        when(trie.autocompletar("")).thenReturn(crearMiLinkedList("init"));

        // Trie autocomplete returns NO matches
        when(trie.autocompletar(prefijo)).thenReturn(crearMiLinkedList());

        // Act
        List<CancionDto> resultados = cancionBusquedaService.autocompletarTitulos(prefijo);

        // Assert
        assertNotNull(resultados);
        assertTrue(resultados.isEmpty());

        verify(cancionRepo, never()).findByTituloInIgnoreCase(anyList());
        verify(cancionMapper, never()).toDto(any(Cancion.class));
    }
}
