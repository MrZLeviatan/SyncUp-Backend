package cancion;

import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.utils.estructuraDatos.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.ArtistaRepo;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.CancionArchivoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para verificar la generación del archivo CSV
 * con las canciones favoritas de un usuario.
 */
class CancionArchivoServiceImplTest {

    // Mocks de los repositorios y utilidades
    @Mock
    private CancionRepo cancionRepo;

    @Mock
    private CancionMapper cancionMapper;

    @Mock
    private ArtistaRepo artistaRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private GrafoDeSimilitud grafoDeSimilitud;

    // Servicio bajo prueba con los mocks inyectados
    @InjectMocks
    private CancionArchivoServiceImpl cancionArchivoService;

    private Usuario usuario;
    private Artista artista;
    private Cancion cancion1;
    private Cancion cancion2;

    @BeforeEach
    void setUp() {
        // Inicializar los mocks de Mockito
        MockitoAnnotations.openMocks(this);

        // Crear artista ficticio
        artista = new Artista();
        artista.setId(1L);
        artista.setNombreArtistico("Artista Prueba");

        // Crear canción 1 de prueba
        cancion1 = new Cancion();
        cancion1.setId(10L);
        cancion1.setTitulo("Canción Test 1");
        cancion1.setGeneroMusical(GeneroMusical.ROCK);
        cancion1.setFechaLanzamiento(LocalDate.of(2023, 1, 10));
        cancion1.setArtistaPrincipal(artista);
        cancion1.setDuracion("03:45");

        // Crear canción 2 de prueba
        cancion2 = new Cancion();
        cancion2.setId(20L);
        cancion2.setTitulo("Canción Test 2");
        cancion2.setGeneroMusical(GeneroMusical.RAP);
        cancion2.setFechaLanzamiento(LocalDate.of(2022, 5, 5));
        cancion2.setArtistaPrincipal(artista);
        cancion2.setDuracion("04:20");

        // Crear usuario con lista de canciones favoritas
        usuario = new Usuario();
        usuario.setId(100L);
        usuario.setCancionesFavoritas(List.of(cancion1, cancion2));
    }

    @Test
    void generarReporteFavoritos_exitoso() throws Exception {
        // Simular que el usuario existe en la base de datos
        when(usuarioRepo.findById(100L)).thenReturn(Optional.of(usuario));

        // Ejecutar el método a probar
        ByteArrayInputStream resultado = cancionArchivoService.generarReporteFavoritos(100L);

        // Leer el contenido del CSV generado
        BufferedReader reader = new BufferedReader(new InputStreamReader(resultado));
        List<String> lineas = reader.lines().collect(Collectors.toList());

        // Verificar que el CSV tenga encabezado + 2 canciones
        assertEquals(3, lineas.size());
        assertEquals("ID;Titulo;Genero;FechaLanzamiento;Artista;Duracion", lineas.get(0));

        // Validar que las canciones estén presentes
        assertTrue(lineas.get(1).contains("Canción Test 1"));
        assertTrue(lineas.get(2).contains("Canción Test 2"));

        // Validar que la información del artista esté en las líneas
        assertTrue(lineas.get(1).contains("Artista Prueba"));
        assertTrue(lineas.get(2).contains("Artista Prueba"));
    }

    @Test
    void generarReporteFavoritos_usuarioNoExiste() {
        // Simular que el usuario no existe en la base de datos
        when(usuarioRepo.findById(999L)).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción esperada
        ElementoNoEncontradoException exception = assertThrows(
                ElementoNoEncontradoException.class,
                () -> cancionArchivoService.generarReporteFavoritos(999L)
        );

        // Comprobar el mensaje de error
        assertEquals("Usuario no encontrado con id: 999", exception.getMessage());
    }
}
