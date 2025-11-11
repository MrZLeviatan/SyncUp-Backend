package usuarioService;

import co.edu.uniquindio.dto.usuario.RegistrarUsuarioDto;
import co.edu.uniquindio.exception.ElementoRepetidoException;
import co.edu.uniquindio.mapper.UsuarioMapper;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para {@link UsuarioServiceImpl}.
 * Verifica el comportamiento del registro de usuarios y la gestión del índice HashMap.
*/
public class RegistroUsuarioTest {

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicializamos el índice manualmente (simula el caché en memoria)
        usuarioService.inicializarCache();
    }

    // Test 1: Registro exitoso de usuario
    @Test
    void registrarUsuario_exitoso() throws ElementoRepetidoException {

        // Creamos el DTO de registro
        RegistrarUsuarioDto dto = new RegistrarUsuarioDto("John Doe", "john123", "1234");

        // Simulamos el mapeo del DTO a entidad
        Usuario usuario = new Usuario();
        usuario.setNombre("John Doe");
        usuario.setUsername("john123");
        usuario.setPassword("1234");

        when(usuarioMapper.toEntity(dto)).thenReturn(usuario);
        when(passwordEncoder.encode("1234")).thenReturn("encodedPassword");
        when(usuarioRepo.save(usuario)).thenReturn(usuario);

        // Ejecutamos el servicio
        usuarioService.registroUsuario(dto);

        // Verificamos que se haya guardado correctamente
        verify(usuarioRepo, times(1)).save(usuario);

        // Validamos que el password fue cifrado
        assertEquals("encodedPassword", usuario.getPassword());

        // Comprobamos que el usuario está en el índice (HashMap)
        assertTrue(usuarioService.getIndiceUsuarios()
                .containsKey("john123"));
    }

    // Test 2: Intentar registrar usuario duplicado
    @Test
    void registrarUsuario_usernameRepetido_lanzaExcepcion() {

        RegistrarUsuarioDto dto = new RegistrarUsuarioDto("Jane", "jane123", "abcd");

        // Simulamos que el índice ya contiene ese username
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setUsername("jane123");

        usuarioService.getIndiceUsuarios().put("jane123", usuarioExistente);

        // Verificamos que se lance la excepción esperada
        assertThrows(ElementoRepetidoException.class,
                () -> usuarioService.registroUsuario(dto));
    }
}

