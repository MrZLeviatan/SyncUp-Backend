package grafoSimilitudes;

import co.edu.uniquindio.dto.playList.PlayListDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.utils.estructuraDatos.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.RadioServiceImpl;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.utils.listasPropias.MiLinkedList;
import co.edu.uniquindio.utils.listasPropias.MiSet;
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
 * {@link RadioServiceImpl#generarDescubrimientoSemanal(Long)}.
 *
 * <p>Verifica la correcta generación de la lista “Descubrimiento Semanal”
 * usando un grafo de similitud simulado y repositorios mockeados.
 */
public class RecomendacionServiceDescubrimientoSemanalTest {

    /** Repositorio de canciones (no utilizado directamente en esta prueba). */

    @Mock
    private GrafoDeSimilitud grafo;

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
    private RadioServiceImpl recomendacionService;

    /** Grafo simulado para controlar el comportamiento de similitudes. */
    private GrafoDeSimilitud grafoMock;

    @BeforeEach
    void setUp() {
        // Inicializa todos los mocks de Mockito
        MockitoAnnotations.openMocks(this);

        // Crea un grafo simulado y lo asigna al servicio directamente (ya que no se inyecta por Spring aquí)
        grafoMock = mock(GrafoDeSimilitud.class);
        recomendacionService = new RadioServiceImpl(grafo,cancionRepo, usuarioRepo, cancionMapper);

        // Inyecta manualmente el grafo simulado al campo privado
        // (porque en la app real se crea en @PostConstruct)
        try {
            var campoGrafo = RadioServiceImpl.class.getDeclaredField("grafo");
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

        Artista artista = new Artista(1L, "Daft Punk", new HashSet<>());

        Cancion favorita = new Cancion(1L, "Harder Better", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "3:45", artista);

        Cancion vecino1 = new Cancion(2L, "Voyager", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "4:12", artista);

        Cancion vecino2 = new Cancion(3L, "Crescendolls", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "3:59", artista);

        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setCancionesFavoritas(new LinkedList<>(List.of(favorita)));

        when(usuarioRepo.findById(10L)).thenReturn(Optional.of(usuario));

        // --- Mock MiSet
        MiSet<Cancion> miSetMock = mock(MiSet.class);
        when(grafoMock.obtenerCanciones()).thenReturn(miSetMock);
        when(miSetMock.iterator()).thenReturn(
                List.of(favorita, vecino1, vecino2).iterator()
        );

        // --- Mock Dijkstra (devuelve MiLinkedList)
        MiLinkedList<Cancion> camino1 = new MiLinkedList<>();
        camino1.add(favorita);
        camino1.add(vecino1);

        MiLinkedList<Cancion> camino2 = new MiLinkedList<>();
        camino2.add(favorita);
        camino2.add(vecino1);
        camino2.add(vecino2);

        when(grafoMock.dijkstra(favorita, vecino1)).thenReturn(camino1);
        when(grafoMock.dijkstra(favorita, vecino2)).thenReturn(camino2);

        when(cancionMapper.toDto(any(Cancion.class))).then(inv -> {
            Cancion c = inv.getArgument(0);
            return new CancionDto(c.getId(), c.getTitulo(), c.getGeneroMusical(), null, null, null, null,null);
        });

        PlayListDto resultado = recomendacionService.generarDescubrimientoSemanal(10L);

        assertNotNull(resultado);
        assertEquals("Descubrimiento Semanal", resultado.nombre());
        assertFalse(resultado.canciones().isEmpty());
    }


    /**
     * Prueba: el usuario no tiene canciones favoritas, la lista debe estar vacía.
     */
    @Test
    @DisplayName("generarDescubrimientoSemanal: usuario sin favoritas")
    void testGenerarDescubrimientoSemanal_SinFavoritas() throws ElementoNoEncontradoException {

        Usuario usuario = new Usuario();
        usuario.setId(20L);
        usuario.setCancionesFavoritas(new LinkedList<>());

        when(usuarioRepo.findById(20L)).thenReturn(Optional.of(usuario));

        MiSet<Cancion> miSetMock = mock(MiSet.class);
        when(grafoMock.obtenerCanciones()).thenReturn(miSetMock);
        when(miSetMock.iterator()).thenReturn(Collections.emptyIterator());

        PlayListDto resultado = recomendacionService.generarDescubrimientoSemanal(20L);

        assertNotNull(resultado);
        assertTrue(resultado.canciones().isEmpty());
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
