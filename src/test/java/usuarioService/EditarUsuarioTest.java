package usuarioService;

import co.edu.uniquindio.dto.usuario.EditarUsuarioDto;
import co.edu.uniquindio.exception.ElemenoNoEncontradoException;
import co.edu.uniquindio.mapper.UsuarioMapper;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class EditarUsuarioTest {

    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private UsuarioRepo usuarioRepo;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioService.getIndiceUsuarios().clear();
    }

    @Test
    void editarUsuario_exitoso() throws ElemenoNoEncontradoException {
        Usuario usuario = new Usuario();
        usuario.setUsername("user1");
        usuario.setNombre("Original");

        usuarioService.getIndiceUsuarios().put("user1", usuario);

        EditarUsuarioDto dto = new EditarUsuarioDto(1L, "Modificado");

        doNothing().when(usuarioMapper).updateUsuarioFromDto(dto, usuario);
        when(usuarioRepo.findById(1L)).thenReturn(java.util.Optional.of(usuario));
        when(usuarioRepo.save(usuario)).thenReturn(usuario);

        usuarioService.editarUsuario(dto);

        verify(usuarioMapper, times(1)).updateUsuarioFromDto(dto, usuario);
        verify(usuarioRepo, times(1)).save(usuario);
        assertEquals(usuario, usuarioService.getIndiceUsuarios().get("user1"));
    }
}
