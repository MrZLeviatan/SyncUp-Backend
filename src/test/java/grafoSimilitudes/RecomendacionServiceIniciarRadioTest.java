package grafoSimilitudes;

import co.edu.uniquindio.dto.RadioDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.exception.ElemenoNoEncontradoException;
import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.impl.RecomendacionServiceImpl;
import co.edu.uniquindio.service.utils.SimilitudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el método {@link RecomendacionServiceImpl#iniciarRadio(Long)}.
 *
 * <p>Incluye:
 * <ul>
 *     <li>Prueba funcional: cuando la canción existe y se generan vecinos correctamente.</li>
 *     <li>Prueba no funcional: cuando el ID de canción no existe en la base de datos.</li>
 * </ul>
 */
public class RecomendacionServiceIniciarRadioTest {

    /** Servicio simulado de similitud. */
    @Mock
    private SimilitudService similitudService;

    /** Repositorio simulado de canciones. */
    @Mock
    private CancionRepo cancionRepo;

    /** Mapper simulado de canciones. */
    @Mock
    private CancionMapper cancionMapper;

    /** Instancia del servicio bajo pruebas. */
    @InjectMocks
    private RecomendacionServiceImpl recomendacionService;

    /** Objeto grafo simulado. */
    private GrafoDeSimilitud grafoMock;

    @BeforeEach
    void setUp() {
        // Inicializa los mocks antes de cada prueba
        MockitoAnnotations.openMocks(this);
        grafoMock = mock(GrafoDeSimilitud.class);
    }

    /**
     * Prueba funcional para verificar que el método genere correctamente una RadioDto
     * cuando el ID de la canción base existe.
     */
    @Test
    @DisplayName("iniciarRadio: éxito - canción encontrada y vecinos generados")
    void testIniciarRadio_Exito() throws ElemenoNoEncontradoException {
        // Crea un artista ficticio
        Artista artista = new Artista(1L, "Daft Punk", new HashSet<>());

        // Crea la canción base
        Cancion cancionBase = new Cancion(1L, "Harder Better", GeneroMusical.RAP,
                LocalDate.now(), "url", "mp3", "3:45", artista);

        // Crea dos canciones vecinas simuladas
        Cancion vecino1 = new Cancion(2L, "One More Time", GeneroMusical.RAP,
                LocalDate.now(), "url", "mp3", "4:05", artista);
        Cancion vecino2 = new Cancion(3L, "Digital Love", GeneroMusical.RAP,
                LocalDate.now(), "url", "mp3", "5:00", artista);

        // Simula la respuesta del repo (la canción existe)
        when(cancionRepo.findById(1L)).thenReturn(Optional.of(cancionBase));

        // Simula el grafo y sus vecinos
        when(similitudService.obtenerGrafo()).thenReturn(grafoMock);
        Map<Cancion, Double> vecinos = new LinkedHashMap<>();
        vecinos.put(vecino1, 0.2);
        vecinos.put(vecino2, 0.4);
        when(grafoMock.obtenerVecinos(cancionBase)).thenReturn(vecinos);

        // Simula el mapper
        when(cancionMapper.toDto(any(Cancion.class)))
                .thenAnswer(inv -> new CancionDto(((Cancion) inv.getArgument(0)).getId(),
                        ((Cancion) inv.getArgument(0)).getTitulo(),((Cancion) inv.getArgument(0)).getGeneroMusical(),
                        ((Cancion) inv.getArgument(0)).getFechaLanzamiento(),((Cancion) inv.getArgument(0)).getUrlCancion(),
                        ((Cancion) inv.getArgument(0)).getUrlPortada(), ((Cancion) inv.getArgument(0)).getArtistaPrincipal().getId()));

        // Ejecuta el método
        RadioDto resultado = recomendacionService.iniciarRadio(1L);

        // Verifica que la respuesta no sea nula
        assertNotNull(resultado);
        // Verifica que el ID base sea correcto
        assertEquals(1L, resultado.idCancionBase());
        // Verifica que la cola tenga los vecinos esperados
        assertEquals(2, resultado.colaReproduccion().size());
        // Verifica el orden de las canciones (por similitud)
        assertEquals("One More Time", resultado.colaReproduccion().get(0).titulo());
        assertEquals("Digital Love", resultado.colaReproduccion().get(1).titulo());
    }

    /**
     * Prueba no funcional para verificar que se lance una excepción cuando
     * el ID de la canción no existe.
     */
    @Test
    @DisplayName("iniciarRadio: error - canción no encontrada")
    void testIniciarRadio_CancionNoEncontrada() {
        // Simula que el repositorio no encuentra la canción
        when(cancionRepo.findById(99L)).thenReturn(Optional.empty());

        // Verifica que se lanza la excepción esperada
        assertThrows(ElemenoNoEncontradoException.class,
                () -> recomendacionService.iniciarRadio(99L));
    }
}
