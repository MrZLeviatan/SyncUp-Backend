package grafoSocial;

import co.edu.uniquindio.dto.usuario.SugerenciaUsuariosDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.utils.estructuraDatos.GrafoSocial;
import co.edu.uniquindio.mapper.UsuarioMapper;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.impl.UsuarioSocialServiceImpl;
import co.edu.uniquindio.utils.listasPropias.MiLinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioSocialServiceSugerenciasTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private GrafoSocial grafoSocial;

    @InjectMocks
    private UsuarioSocialServiceImpl usuarioSocialService;

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
    void testObtenerSugerenciasExitosas() throws ElementoNoEncontradoException {

        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioBase));

        // 🔥 MiLinkedList en lugar de List
        MiLinkedList<Usuario> lista = new MiLinkedList<>();
        lista.add(amigo1);
        lista.add(amigo2);

        when(grafoSocial.obtenerAmigosDeAmigos(usuarioBase)).thenReturn(lista);

        when(usuarioMapper.toDtoSugerenciaUsuarios(amigo1))
                .thenReturn(new SugerenciaUsuariosDto(2L, "Ana Amiga", "anaA"));
        when(usuarioMapper.toDtoSugerenciaUsuarios(amigo2))
                .thenReturn(new SugerenciaUsuariosDto(3L, "Luis Recomendado", "luisR"));

        List<SugerenciaUsuariosDto> sugerencias = usuarioSocialService.obtenerSugerencias(1L);

        assertEquals(2, sugerencias.size());

        List<String> nombres = sugerencias.stream().map(SugerenciaUsuariosDto::nombre).toList();

        assertTrue(nombres.contains("Ana Amiga"));
        assertTrue(nombres.contains("Luis Recomendado"));

        verify(grafoSocial).obtenerAmigosDeAmigos(usuarioBase);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el usuario base no existe")
    void testObtenerSugerenciasUsuarioNoEncontrado() {

        when(usuarioRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ElementoNoEncontradoException.class,
                () -> usuarioSocialService.obtenerSugerencias(1L));

        verify(grafoSocial, never()).obtenerAmigosDeAmigos(any());
    }

    @Test
    @DisplayName("Debería devolver lista vacía si no hay sugerencias disponibles")
    void testObtenerSugerenciasVacia() throws ElementoNoEncontradoException {

        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioBase));

        // 🔥 MiLinkedList vacía
        MiLinkedList<Usuario> listaVacia = new MiLinkedList<>();
        when(grafoSocial.obtenerAmigosDeAmigos(usuarioBase)).thenReturn(listaVacia);

        List<SugerenciaUsuariosDto> sugerencias = usuarioSocialService.obtenerSugerencias(1L);

        assertTrue(sugerencias.isEmpty());
    }
}
