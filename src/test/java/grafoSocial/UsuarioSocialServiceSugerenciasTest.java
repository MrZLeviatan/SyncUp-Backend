package grafoSocial;


import co.edu.uniquindio.dto.usuario.SugerenciaUsuariosDto;
import co.edu.uniquindio.exception.ElemenoNoEncontradoException;
import co.edu.uniquindio.graph.GrafoSocial;
import co.edu.uniquindio.mapper.UsuarioMapper;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.UsuarioSocialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de prueba que valida la obtención de sugerencias de usuarios.
 */
class UsuarioSocialServiceSugerenciasTest {

    @Mock
    private UsuarioRepo usuarioRepo; // Simula la base de datos de usuarios.

    @Mock
    private UsuarioMapper usuarioMapper; // Transforma Usuario -> DTO.

    @Mock
    private GrafoSocial grafoSocial; // Simula el grafo social.

    @InjectMocks
    private UsuarioSocialServiceImpl usuarioSocialService; // Servicio bajo prueba.

    private Usuario usuarioBase;
    private Usuario amigo1;
    private Usuario amigo2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuarioBase = new Usuario();
        usuarioBase.setId(1L);
        usuarioBase.setNombre("Carlos Base");
        usuarioBase.setUsername("carlosB");


        amigo1 = new Usuario();
        amigo1.setId(2L);
        amigo1.setNombre("Ana Amiga");
        amigo1.setUsername("anaA");


        amigo2 = new Usuario();
        amigo2.setId(3L);
        amigo2.setNombre("Luis Recomendado");
        amigo2.setUsername("luisR");

    }

    @Test
    @DisplayName("Debería devolver sugerencias de amigos correctamente")
    void testObtenerSugerenciasExitosas() throws ElemenoNoEncontradoException {
        // Arrange: simula que el usuario base existe.
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioBase));

        // Simula que el grafo devuelve una lista de amigos sugeridos.
        when(grafoSocial.obtenerAmigosDeAmigos(usuarioBase)).thenReturn(List.of(amigo1, amigo2));

        // Define el mapeo a DTO de cada usuario sugerido.
        when(usuarioMapper.toDtoSugerenciaUsuarios(amigo1))
                .thenReturn(new SugerenciaUsuariosDto(2L, "Ana Amiga", "anaA"));
        when(usuarioMapper.toDtoSugerenciaUsuarios(amigo2))
                .thenReturn(new SugerenciaUsuariosDto(3L, "Luis Recomendado", "luisR"));

        // Act: obtiene las sugerencias.
        List<SugerenciaUsuariosDto> sugerencias = usuarioSocialService.obtenerSugerencias(1L);

        // Assert: válida la cantidad y contenido.
        assertEquals(2, sugerencias.size(), "Deberían haberse devuelto 2 sugerencias.");

        List<String> nombres = sugerencias.stream()
                .map(SugerenciaUsuariosDto::nombre)
                .toList();

        assertTrue(nombres.contains("Ana Amiga"), "Debería contener 'Ana Amiga'");
        assertTrue(nombres.contains("Luis Recomendado"), "Debería contener 'Luis Recomendado'");
        assertEquals(2, nombres.size(), "Deberían ser exactamente 2 sugerencias");


        // Verifica que se haya invocado el método del grafo.
        verify(grafoSocial).obtenerAmigosDeAmigos(usuarioBase);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el usuario base no existe")
    void testObtenerSugerenciasUsuarioNoEncontrado() {
        // Arrange
        when(usuarioRepo.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ElemenoNoEncontradoException.class,
                () -> usuarioSocialService.obtenerSugerencias(1L));

        // Verifica que no se haya llamado al grafo.
        verify(grafoSocial, never()).obtenerAmigosDeAmigos(any());
    }

    @Test
    @DisplayName("Debería devolver lista vacía si no hay sugerencias disponibles")
    void testObtenerSugerenciasVacia() throws ElemenoNoEncontradoException {
        // Arrange
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(grafoSocial.obtenerAmigosDeAmigos(usuarioBase)).thenReturn(List.of());

        // Act
        List<SugerenciaUsuariosDto> sugerencias = usuarioSocialService.obtenerSugerencias(1L);

        // Assert
        assertTrue(sugerencias.isEmpty(), "La lista de sugerencias debería estar vacía.");
    }
}
