package cancion;

import co.edu.uniquindio.dto.cancion.RegistrarCancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.utils.estructuraDatos.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.ArtistaRepo;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.impl.CancionServiceImpl;
import co.edu.uniquindio.utils.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancionServiceImplTest {

    @Mock
    private CancionRepo cancionRepo;

    @Mock
    private CancionMapper cancionMapper;

    @Mock
    private ArtistaRepo artistaRepo;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MultipartFile archivoCancion;

    @Mock
    private MultipartFile imagenPortada;

    @Mock
    private GrafoDeSimilitud grafoDeSimilitud;


    @InjectMocks
    private CancionServiceImpl cancionService;




    private Artista artista;
    private RegistrarCancionDto dto;
    private Cancion cancion;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);



        // Crear artista simulado
        artista = new Artista();
        artista.setId(1L);
        artista.setNombreArtistico("Artista Test");

        // Cargar archivo MP3 real desde src/test/resources
        File mp3File = new File("src/test/resources/prueba.mp3");
        archivoCancion = new MockMultipartFile(
                "archivoCancion",
                mp3File.getName(),
                "audio/mpeg",
                new FileInputStream(mp3File)
        );

        // Cargar imagen de portada desde src/test/resources
        File imgFile = new File("src/test/resources/imagen.png");
        imagenPortada = new MockMultipartFile(
                "imagenPortada",
                imgFile.getName(),
                "image/png",
                new FileInputStream(imgFile)
        );

        // DTO de prueba
        dto = new RegistrarCancionDto(
                "Cancion Test",
                GeneroMusical.RAP,
                LocalDate.of(2025, 11, 11),
                archivoCancion,
                imagenPortada,
                1L
        );

        // Canción simulada
        cancion = new Cancion();
        cancion.setTitulo(dto.titulo());
        cancion.setGeneroMusical(dto.generoMusical());
        cancion.setFechaLanzamiento(dto.fechaLanzamiento());
    }



    @Test
    void agregarCancion_exitoso() throws Exception {
        // Simular repositorio de artista
        when(artistaRepo.findById(1L)).thenReturn(Optional.of(artista));

        // Simular subida a Cloudinary
        when(cloudinaryService.uploadImage(imagenPortada)).thenReturn("urlImagen");
        when(cloudinaryService.uploadMp3(archivoCancion)).thenReturn("urlMp3");

        // Simular mapper
        when(cancionMapper.toEntity(dto)).thenReturn(cancion);

        // Ejecutar método
        cancionService.agregarCancion(dto);

        // Capturar lo que se guardó en la base de datos
        ArgumentCaptor<Cancion> captor = ArgumentCaptor.forClass(Cancion.class);
        verify(cancionRepo, times(1)).save(captor.capture());

        Cancion guardada = captor.getValue();

        // Verificar que se asignaron correctamente los valores
        assertEquals("urlImagen", guardada.getUrlPortada());
        assertEquals("urlMp3", guardada.getUrlCancion());
        assertEquals(artista, guardada.getArtistaPrincipal());
        assertNotNull(guardada.getDuracion()); // Se calculó la duración
    }

    @Test
    void agregarCancion_artistaNoExiste() {
        when(artistaRepo.findById(1L)).thenReturn(Optional.empty());

        ElementoNoEncontradoException thrown = assertThrows(
                ElementoNoEncontradoException.class,
                () -> cancionService.agregarCancion(dto)
        );

        assertEquals("Artista no  encontrado", thrown.getMessage());
    }

}