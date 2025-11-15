package grafoSimilitudes;

import co.edu.uniquindio.dto.playList.RadioDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.RecomendacionServiceImpl;
import co.edu.uniquindio.utils.collections.MiMap;
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

    /** Repositorio simulado de canciones. */
    @Mock
    private CancionRepo cancionRepo;

    /** Repositorio simulado de usuarios (necesario por la clase, aunque no se usa aquí). */
    @Mock
    private UsuarioRepo usuarioRepo;

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

        // Crea una nueva instancia del servicio con dependencias
        recomendacionService = new RecomendacionServiceImpl(cancionRepo, usuarioRepo, cancionMapper);

        // Inyecta el grafo simulado al campo privado del servicio
        try {
            var field = RecomendacionServiceImpl.class.getDeclaredField("grafo");
            field.setAccessible(true);
            field.set(recomendacionService, grafoMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prueba funcional para verificar que el método genere correctamente una RadioDto
     * cuando el ID de la canción base existe.
     */
    @Test
    @DisplayName("✔ iniciarRadio: éxito - canción encontrada y vecinos generados")
    void testIniciarRadio_Exito() throws ElementoNoEncontradoException {

        Artista artista = new Artista(1L, "Daft Punk", new HashSet<>());

        Cancion cancionBase = new Cancion(1L, "Harder Better", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "3:45", artista);

        Cancion vecino1 = new Cancion(2L, "One More Time", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "4:05", artista);

        Cancion vecino2 = new Cancion(3L, "Digital Love", GeneroMusical.ELECTRONICA,
                LocalDate.now(), "url", "mp3", "5:00", artista);

        // --- Mock repositorio canción
        when(cancionRepo.findById(1L)).thenReturn(Optional.of(cancionBase));

        // --- Mock MiMap (vecinos)
        MiMap<Cancion, Double> miMapMock = mock(MiMap.class);
        when(grafoMock.obtenerVecinos(cancionBase)).thenReturn(miMapMock);

        // --- Simular iteración sobre MiMap (en tu código se usa for(MiMap.Par...))
        MiMap.Par<Cancion, Double> par1 = new MiMap.Par<>(vecino1, 0.2);
        MiMap.Par<Cancion, Double> par2 = new MiMap.Par<>(vecino2, 0.4);

        when(miMapMock.iterator()).thenReturn(List.of(par1, par2).iterator());

        // --- Mock mapper
        when(cancionMapper.toDto(any(Cancion.class))).then(inv -> {
            Cancion c = inv.getArgument(0);
            return new CancionDto(
                    c.getId(), c.getTitulo(), c.getGeneroMusical(),
                    c.getFechaLanzamiento(), c.getUrlCancion(),
                    c.getUrlPortada(), c.getArtistaPrincipal().getId()
            );
        });

        RadioDto resultado = recomendacionService.iniciarRadio(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.idCancionBase());
        assertEquals(2, resultado.colaReproduccion().size());
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
        assertThrows(ElementoNoEncontradoException.class,
                () -> recomendacionService.iniciarRadio(99L));
    }
}
