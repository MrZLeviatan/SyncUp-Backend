package grafoSocial;

import co.edu.uniquindio.dto.usuario.UsuarioConexionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de prueba unitaria que verifica las operaciones de conexión y desconexión social.
 */
class UsuarioSocialServiceSeguirTest {

    @Mock
    private UsuarioRepo usuarioRepo; // Simula el repositorio para evitar acceso a la base real.

    @Mock
    private UsuarioMapper usuarioMapper; // No se usa directamente aquí, pero requerido por el servicio.

    @Mock
    private GrafoSocial grafoSocial; // Simula el grafo de relaciones en memoria.
    // Mock for the social graph structure.

    @InjectMocks
    private UsuarioSocialServiceImpl usuarioSocialService; // Servicio bajo prueba (Unit Under Test).

    private Usuario principal;
    private Usuario objetivo;

    @BeforeEach
    void setUp() {
        // Inicializa los mocks antes de cada prueba.
        MockitoAnnotations.openMocks(this);

        // Crea usuarios simulados para las pruebas.
        principal = new Usuario();
        principal.setId(1L);
        principal.setNombre("Juan Principal");
        principal.setUsername("juan123");


        objetivo = new Usuario();
        objetivo.setId(2L);
        objetivo.setNombre("Pedro Objetivo");
        objetivo.setUsername("pedro456");

    }

    @Test
    @DisplayName("Debería permitir que un usuario siga a otro correctamente")
    void testSeguirUsuarioExitoso() throws ElementoNoEncontradoException {
        // Arrange: configura las respuestas simuladas del repositorio.
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(principal));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(objetivo));

        // Crea el DTO que representa la acción de seguir.
        UsuarioConexionDto dto = new UsuarioConexionDto(1L, 2L);

        // Act: ejecuta el método del servicio.
        usuarioSocialService.seguirUsuario(dto);

        // Assert: verifica que la relación se haya registrado correctamente.
        assertTrue(principal.getUsuariosSeguidos().contains(objetivo),
                "El usuario principal debería tener al objetivo en su lista de seguidos.");

        // Verifica que el repositorio y el grafo se hayan actualizado.
        verify(usuarioRepo).save(principal);
        verify(grafoSocial).conectarUsuarios(principal, objetivo);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el usuario principal no existe")
    void testSeguirUsuarioPrincipalNoEncontrado() {
        // Arrange: simula que el usuario principal no existe.
        when(usuarioRepo.findById(1L)).thenReturn(Optional.empty());

        UsuarioConexionDto dto = new UsuarioConexionDto(1L, 2L);

        // Act + Assert: espera la excepción de elemento no encontrado.
        assertThrows(ElementoNoEncontradoException.class, () -> usuarioSocialService.seguirUsuario(dto));

        // Verifica que no se haya intentado guardar nada.
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si el usuario objetivo no existe")
    void testSeguirUsuarioObjetivoNoEncontrado() {
        // Arrange: principal existe, objetivo no.
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(principal));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.empty());

        UsuarioConexionDto dto = new UsuarioConexionDto(1L, 2L);

        // Act + Assert
        assertThrows(ElementoNoEncontradoException.class, () -> usuarioSocialService.seguirUsuario(dto));

        // Verifica que no se haya guardado ningún cambio.
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    @DisplayName("Debería permitir dejar de seguir correctamente")
    void testDejarDeSeguirExitoso() throws ElementoNoEncontradoException {
        // Arrange: configura usuarios conectados previamente.
        principal.seguirUsuario(objetivo);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(principal));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(objetivo));

        UsuarioConexionDto dto = new UsuarioConexionDto(1L, 2L);

        // Act
        usuarioSocialService.dejarDeSeguirUsuario(dto);

        // Assert
        assertFalse(principal.getUsuariosSeguidos().contains(objetivo),
                "El usuario principal no debería seguir al objetivo.");

        verify(usuarioRepo).save(principal);
        verify(grafoSocial).desconectarUsuarios(principal, objetivo);
    }
}

