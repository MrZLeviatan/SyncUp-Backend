package auth;

import co.edu.uniquindio.dto.LoginDto;
import co.edu.uniquindio.dto.TokenDto;
import co.edu.uniquindio.exception.ElementoNoCoincideException;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.models.Admin;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.AdminRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.security.JWTUtils;
import co.edu.uniquindio.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private AdminRepo adminRepo;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Prueba: Inicio de sesión exitoso para un Usuario
     */
    @Test
    void loginUsuarioExitoso() throws ElementoNoEncontradoException, ElementoNoCoincideException {
        // Crear usuario falso
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("leviatan");
        usuario.setPassword("hashedpass");

        // Simular base de datos y utilidades
        when(usuarioRepo.findByUsername("leviatan")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "hashedpass")).thenReturn(true);
        when(jwtUtils.generarTokenLogin(usuario)).thenReturn(new java.util.HashMap<>());
        when(jwtUtils.generateToken(anyString(), anyMap())).thenReturn("fake-jwt-token");

        // Ejecutar login
        TokenDto tokenDto = authService.login(new LoginDto("leviatan", "1234"));

        // Verificar resultado
        assertNotNull(tokenDto);
        assertEquals("fake-jwt-token", tokenDto.getToken());
        verify(usuarioRepo, times(1)).findByUsername("leviatan");
        verify(adminRepo, never()).findByUsername(anyString());
    }

    /**
     * Prueba: Inicio de sesión exitoso para un Admin
     */
    @Test
    void loginAdminExitoso() throws ElementoNoEncontradoException, ElementoNoCoincideException {
        // Crear admin falso
        Admin admin = new Admin();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setPassword("secure");

        // Simular repositorios y validación
        when(usuarioRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(adminRepo.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("1234", "secure")).thenReturn(true);
        when(jwtUtils.generarTokenLogin(admin)).thenReturn(new java.util.HashMap<>());
        when(jwtUtils.generateToken(anyString(), anyMap())).thenReturn("token-admin");

        // Ejecutar login
        TokenDto tokenDto = authService.login(new LoginDto("admin", "1234"));

        // Verificar token
        assertNotNull(tokenDto);
        assertEquals("token-admin", tokenDto.getToken());
    }

    /**
     * Prueba: Contraseña inválida debe lanzar ElementoNoCoincideException
     */
    @Test
    void loginPasswordIncorrecta() {
        Usuario usuario = new Usuario();
        usuario.setUsername("leviatan");
        usuario.setPassword("correcto");

        when(usuarioRepo.findByUsername("leviatan")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "correcto")).thenReturn(false);

        assertThrows(ElementoNoCoincideException.class,
                () -> authService.login(new LoginDto("leviatan", "1234")));
    }

    /**
     * Prueba: Usuario inexistente debe lanzar ElementoNoEncontradoException
     */
    @Test
    void loginUsuarioNoExiste() {
        when(usuarioRepo.findByUsername("ghost")).thenReturn(Optional.empty());
        when(adminRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ElementoNoEncontradoException.class,
                () -> authService.login(new LoginDto("ghost", "pass")));
    }
}
