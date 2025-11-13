package grafoSimilitudes;

import co.edu.uniquindio.dto.playList.PlayListDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.RecomendacionServiceImpl;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
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
 * Pruebas unitarias para el método
 * {@link RecomendacionServiceImpl#generarDescubrimientoSemanal(Long)}.
 *
 * <p>Verifica la correcta generación de la lista “Descubrimiento Semanal”
 * usando un grafo de similitud simulado y repositorios mockeados.
 */
public class RecomendacionServiceDescubrimientoSemanalTest {

    /** Repositorio de canciones (no utilizado directamente en esta prueba). */
    @Mock
    private CancionRepo cancionRepo;

    /** Repositorio de usuarios, usado para simular la búsqueda por ID. */
    @Mock
    private UsuarioRepo usuarioRepo;

    /** Mapper que convierte entidades Cancion a DTOs CancionDto. */
    @Mock
    private CancionMapper cancionMapper;

    /** Servicio bajo prueba, con dependencias inyectadas automáticamente. */
    @InjectMocks
    private RecomendacionServiceImpl recomendacionService;

    /** Grafo simulado para controlar el comportamiento de similitudes. */
    private GrafoDeSimilitud grafoMock;

    @BeforeEach
    void setUp() {
        // Inicializa todos los mocks de Mockito
        MockitoAnnotations.openMocks(this);

        // Crea un grafo simulado y lo asigna al servicio directamente (ya que no se inyecta por Spring aquí)
        grafoMock = mock(GrafoDeSimilitud.class);
        recomendacionService = new RecomendacionServiceImpl(cancionRepo, usuarioRepo, cancionMapper);

        // Inyecta manualmente el grafo simulado al campo privado
        // (porque en la app real se crea en @PostConstruct)
        try {
            var campoGrafo = RecomendacionServiceImpl.class.getDeclaredField("grafo");
            campoGrafo.setAccessible(true);
            campoGrafo.set(recomendacionService, grafoMock);
        } catch (Exception e) {
            throw new RuntimeException("Error al inyectar grafo simulado", e);
        }
    }

    /**
     * Prueba funcional: el usuario tiene canciones favoritas y se generan recomendaciones correctamente.
     */
    @Test
    @DisplayName("generarDescubrimientoSemanal: éxito - playlist generada correctamente")
    void testGenerarDescubrimientoSemanal_Exito() throws ElementoNoEncontradoException {
        // Crea un artista de ejemplo
        Artista artista = new Artista(1L, "Daft Punk", new HashSet<>());

        // Crea canciones de ejemplo
        Cancion favorita = new Cancion(1L, "Harder Better", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "3:45", artista);

        Cancion vecino1 = new Cancion(2L, "Voyager", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "4:12", artista);

        Cancion vecino2 = new Cancion(3L, "Crescendolls", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "3:59", artista);

        // Crea un usuario con una favorita
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setCancionesFavoritas(new LinkedList<>(List.of(favorita)));

        // Simula que el repositorio devuelve el usuario cuando se busca por su ID
        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuario));

        // Simula que el grafo contiene las canciones del sistema
        when(grafoMock.obtenerCanciones()).thenReturn(new HashSet<>(List.of(favorita, vecino1, vecino2)));

        // Simula el camino más corto usando Dijkstra: favorita -> vecino1 -> vecino2
        when(grafoMock.dijkstra(favorita, vecino1)).thenReturn(List.of(favorita, vecino1));
        when(grafoMock.dijkstra(favorita, vecino2)).thenReturn(List.of(favorita, vecino1, vecino2));

        // Simula la conversión Cancion → CancionDto
        when(cancionMapper.toDto(any(Cancion.class))).thenAnswer(invocacion -> {
            Cancion c = invocacion.getArgument(0);
            return new CancionDto(c.getId(), c.getTitulo(), c.getGeneroMusical(), null, null, null, null);
        });

        // Ejecuta el método
        PlayListDto resultado = recomendacionService.generarDescubrimientoSemanal(10L);

        // Validaciones
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals("Descubrimiento Semanal", resultado.nombre(), "El nombre de la playlist es incorrecto");
        assertFalse(resultado.canciones().isEmpty(), "Debe haber canciones recomendadas");
        assertTrue(resultado.canciones().size() <= 15, "Debe haber máximo 15 canciones");
    }

    /**
     * Prueba: el usuario no tiene canciones favoritas, la lista debe estar vacía.
     */
    @Test
    @DisplayName("generarDescubrimientoSemanal: usuario sin favoritas")
    void testGenerarDescubrimientoSemanal_SinFavoritas() throws ElementoNoEncontradoException {
        // Crea usuario sin favoritas
        Usuario usuario = new Usuario();
        usuario.setId(20L);
        usuario.setCancionesFavoritas(new LinkedList<>());

        // Simula repositorio devolviendo al usuario
        when(usuarioRepo.findById(20L)).thenReturn(Optional.of(usuario));

        // Simula grafo vacío
        when(grafoMock.obtenerCanciones()).thenReturn(Set.of());

        // Ejecuta método
        PlayListDto resultado = recomendacionService.generarDescubrimientoSemanal(20L);

        // Verificaciones
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals("Descubrimiento Semanal", resultado.nombre(), "Nombre incorrecto");
        assertTrue(resultado.canciones().isEmpty(), "La lista de canciones debe estar vacía");
    }

    /**
     * Prueba: cuando el ID de usuario no existe, se lanza excepción.
     */
    @Test
    @DisplayName("generarDescubrimientoSemanal: error - usuario no encontrado")
    void testGenerarDescubrimientoSemanal_UsuarioNoEncontrado() {
        // Simula que el repositorio no encuentra al usuario
        when(usuarioRepo.findById(99L)).thenReturn(Optional.empty());

        // Verifica que lanza la excepción esperada
        assertThrows(ElementoNoEncontradoException.class, () ->
                        recomendacionService.generarDescubrimientoSemanal(99L),
                "Debe lanzar excepción si el usuario no existe");
    }
}
