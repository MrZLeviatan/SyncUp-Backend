package cancion;

import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.graph.TrieAutocompletado;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.impl.CancionBusquedaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para la clase {@link CancionBusquedaServiceImpl}.
 * Verifica que el servicio de autocompletado de títulos funcione correctamente
 * utilizando un Trie y un repositorio simulado (mocked).
 */
class CancionBusquedaServiceImplTest {

    // Simulamos dependencias del servicio (repositorio, mapper y Trie)
    @Mock
    private CancionRepo cancionRepo;

    @Mock
    private CancionMapper cancionMapper;

    @Mock
    private TrieAutocompletado trie;

    // Inyectamos los mocks dentro del servicio que vamos a probar
    @InjectMocks
    private CancionBusquedaServiceImpl cancionBusquedaService;

    @BeforeEach
    void setUp() {
        // Inicializa los mocks antes de cada prueba
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Prueba principal:
     * Verifica que el servicio devuelva una lista de canciones coincidentes
     * cuando el Trie y el repositorio retornan resultados válidos.
     */
    @Test
    void testAutocompletarTitulos_CoincidenciasEncontradas() {
        // Arrange (Preparación)
        String prefijo = "Ama";

        // Creamos canciones simuladas
        Cancion cancion1 = new Cancion(1L, "Amar y querer", GeneroMusical.ELECTRONICA, LocalDate.of(2020, 5, 10), "url1", "portada1", null, null);
        Cancion cancion2 = new Cancion(2L, "Amargo adiós", GeneroMusical.ROCK, LocalDate.of(2021, 2, 15), "url2", "portada2", null, null);

        // Simulamos que el Trie devuelve dos coincidencias de títulos
        when(trie.autocompletar("")).thenReturn(List.of("init")); // Para evitar reinicialización
        when(trie.autocompletar(prefijo)).thenReturn(List.of("Amar y querer", "Amargo adiós"));

        // Simulamos que el repositorio devuelve las canciones correspondientes
        when(cancionRepo.findByTituloInIgnoreCase(List.of("Amar y querer", "Amargo adiós")))
                .thenReturn(List.of(cancion1, cancion2));

        // Simulamos que el mapper convierte las entidades a DTO
        when(cancionMapper.toDto(cancion1)).thenReturn(new CancionDto(
                1L, "Amar y querer", GeneroMusical.ELECTRONICA, LocalDate.of(2020, 5, 10), "url1", "portada1", 1L));

        when(cancionMapper.toDto(cancion2)).thenReturn(new CancionDto(
                2L, "Amargo adiós", GeneroMusical.ROCK, LocalDate.of(2021, 2, 15), "url2", "portada2", 1L));

        // Act (Ejecución)
        List<CancionDto> resultados = cancionBusquedaService.autocompletarTitulos(prefijo);

        // Assert (Verificación)
        assertNotNull(resultados, "La lista de resultados no debe ser nula");
        assertEquals(2, resultados.size(), "Debe devolver 2 canciones coincidentes");
        assertEquals("Amar y querer", resultados.get(0).titulo());
        assertEquals("Amargo adiós", resultados.get(1).titulo());

        // Verificamos que los métodos fueron llamados correctamente
        verify(trie, times(1)).autocompletar(prefijo);
        verify(cancionRepo, times(1)).findByTituloInIgnoreCase(anyList());
        verify(cancionMapper, times(2)).toDto(any(Cancion.class));
    }

    /**
     * Prueba secundaria:
     * Verifica que el servicio devuelva una lista vacía si no hay coincidencias.
     */
    @Test
    void testAutocompletarTitulos_SinCoincidencias() {
        // Arrange
        String prefijo = "XYZ";
        when(trie.autocompletar("")).thenReturn(List.of("init"));
        when(trie.autocompletar(prefijo)).thenReturn(List.of());

        // Act
        List<CancionDto> resultados = cancionBusquedaService.autocompletarTitulos(prefijo);

        // Assert
        assertNotNull(resultados);
        assertTrue(resultados.isEmpty(), "Si no hay coincidencias, la lista debe estar vacía");

        verify(cancionRepo, never()).findByTituloInIgnoreCase(anyList());
        verify(cancionMapper, never()).toDto(any(Cancion.class));
    }
}
