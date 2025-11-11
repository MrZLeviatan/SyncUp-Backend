package grafoSimilitudes;

import co.edu.uniquindio.dto.PlayListDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
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
 * Clase de pruebas unitarias para el método
 * {@link RecomendacionServiceImpl#generarDescubrimientoSemanal(Usuario)}.
 *
 * <p>Simula el comportamiento del grafo de similitud, el mapper y las entidades
 * para verificar la correcta generación de la lista de reproducción "Descubrimiento Semanal".
 *
 * <p>Incluye:
 * <ul>
 *     <li>Prueba funcional: el usuario tiene canciones favoritas, y se genera una playlist.</li>
 *     <li>Prueba no funcional: el usuario no tiene canciones favoritas, y la playlist resulta vacía.</li>
 * </ul>
 */
public class RecomendacionServiceDescubrimientoSemanalTest {

    /** Servicio que gestiona el grafo de similitud (simulado con Mockito). */
    @Mock
    private SimilitudService similitudService;

    /** Repositorio de canciones (requerido por la clase, aunque no se usa en esta prueba). */
    @Mock
    private CancionRepo cancionRepo;

    /** Mapper encargado de convertir entidades {@link Cancion} a {@link CancionDto}. */
    @Mock
    private CancionMapper cancionMapper;

    /** Servicio bajo prueba, con dependencias inyectadas por Mockito. */
    @InjectMocks
    private RecomendacionServiceImpl recomendacionService;

    /** Grafo de similitud simulado. */
    private GrafoDeSimilitud grafoMock;

    /**
     * Se ejecuta antes de cada prueba.
     * Inicializa los mocks y el grafo simulado.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        grafoMock = mock(GrafoDeSimilitud.class);
    }

    /**
     * Prueba funcional.
     *
     * <p>Verifica que se genera una playlist válida de “Descubrimiento Semanal”
     * cuando el usuario tiene canciones favoritas registradas.
     */
    @Test
    @DisplayName("generarDescubrimientoSemanal: éxito - playlist generada correctamente")
    void testGenerarDescubrimientoSemanal_Exito() {
        // Crea un artista de ejemplo
        Artista artista = new Artista(1L, "Daft Punk", new HashSet<>());

        // Crea una canción favorita del usuario
        Cancion favorita = new Cancion(1L, "Harder Better", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "3:45", artista);

        // Canciones vecinas (similares)
        Cancion vecino1 = new Cancion(2L, "Voyager", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "4:12", artista);
        Cancion vecino2 = new Cancion(3L, "Crescendolls", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "3:59", artista);

        // Crea un usuario con su lista de canciones favoritas (LinkedList)
        Usuario usuario = new Usuario();
        usuario.setCancionesFavoritas(new LinkedList<>(List.of(favorita)));

        // Simula el comportamiento del servicio de similitud
        when(similitudService.obtenerGrafo()).thenReturn(grafoMock);

        // Define los vecinos de la canción favorita
        Map<Cancion, Double> vecinos = new LinkedHashMap<>();
        vecinos.put(vecino1, 0.2);
        vecinos.put(vecino2, 0.3);

        // El grafo devuelve los vecinos simulados
        when(grafoMock.obtenerVecinos(favorita)).thenReturn(vecinos);

        // Simula el mapper para transformar Cancion → CancionDto
        when(cancionMapper.toDto(any(Cancion.class))).thenAnswer(invocacion -> {
            Cancion c = invocacion.getArgument(0);
            return new CancionDto(c.getId(), c.getTitulo(), GeneroMusical.ELECTRONICA,null, null, null,null );
        });

        // Ejecuta el método a probar
        PlayListDto resultado = recomendacionService.generarDescubrimientoSemanal(usuario);

        // Verificaciones
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals("Descubrimiento Semanal", resultado.nombre(), "El nombre de la playlist es incorrecto");
        assertEquals(2, resultado.canciones().size(), "Debe haber 2 canciones recomendadas");
    }

    /**
     * Prueba no funcional.
     *
     * <p>Verifica que el método maneja correctamente el caso
     * cuando el usuario no tiene canciones favoritas registradas.
     */
    @Test
    @DisplayName("generarDescubrimientoSemanal: error - usuario sin favoritas")
    void testGenerarDescubrimientoSemanal_SinFavoritas() {
        // Crea un usuario con lista vacía de canciones favoritas (LinkedList)
        Usuario usuario = new Usuario();
        usuario.setCancionesFavoritas(new LinkedList<>());

        // Simula el grafo vacío
        when(similitudService.obtenerGrafo()).thenReturn(grafoMock);

        // Ejecuta el método
        PlayListDto resultado = recomendacionService.generarDescubrimientoSemanal(usuario);

        // Validaciones
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertTrue(resultado.canciones().isEmpty(), "La lista de canciones debe estar vacía");
    }
}

