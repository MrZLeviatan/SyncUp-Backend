package co.edu.uniquindio.constants;

import co.edu.uniquindio.models.Admin;
import co.edu.uniquindio.models.Artista;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.models.enums.GeneroMusical;
import co.edu.uniquindio.repo.AdminRepo;
import co.edu.uniquindio.repo.ArtistaRepo;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;


@Component
@RequiredArgsConstructor
public class DatosIniciales  implements CommandLineRunner {


    private final PasswordEncoder passwordEncoder;

    private final AdminRepo adminRepo;

    private final UsuarioRepo usuarioRepo;

    private final ArtistaRepo artistaRepo;

    private final CancionRepo cancionRepo;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

/**
        // Plantilla para quemar Admin
        Admin admin = new Admin();
        admin.setNombre("admin");
        admin.setUsername("admin123");
        admin.setPassword(passwordEncoder.encode("admin"));
        adminRepo.save(admin);

        // Plantilla para quemar Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre("usuario");
        usuario.setUsername("usuario123");
        usuario.setPassword(passwordEncoder.encode("usuario"));
        usuario.setCancionesFavoritas(new LinkedList<>());
        usuario.setUsuariosSeguidos(new LinkedList<>());
        usuarioRepo.save(usuario);

        // Plantilla para quemar artista
        Artista artista = new Artista();
        artista.setNombreArtistico("artista");
        artista.setCanciones(new HashSet<>());
        artistaRepo.save(artista);

        // Plantilla para quemar canciones
        Cancion cancion = new Cancion();
        cancion.setTitulo("cancion");
        cancion.setGeneroMusical(GeneroMusical.MERENGUE);
        cancion.setFechaLanzamiento(LocalDate.now());
        cancion.setUrlCancion(null);
        cancion.setUrlPortada(null);
        cancion.setDuracion(null);
        cancionRepo.save(cancion);


 */
    }
}
