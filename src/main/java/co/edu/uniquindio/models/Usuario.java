package co.edu.uniquindio.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.LinkedList;
import java.util.List;

/**
 * Representa a un usuario estándar registrado dentro de la plataforma.
 *
 * <p>Hereda de la clase abstracta {@link Persona}, incorporando propiedades esenciales
 * para identificación y autenticación tales como nombre, username y credenciales de acceso.
 *
 * <p>Además de los atributos heredados, esta entidad modela las interacciones sociales
 * del usuario con el ecosistema musical, incluyendo canciones favoritas, álbumes que le
 * gustan, artistas preferidos y otros usuarios a los que sigue.
 *
 * <p>Su diseño permite gestionar dinámicamente gustos, preferencias y relaciones del usuario
 * mediante asociaciones bidireccionales y tablas de unión optimizadas.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
@Comment("Entidad que representa a los usuarios registrados del sistema.")
public class Usuario extends Persona {

    /**
     * URL pública de la imagen de perfil del usuario.
     *
     * <p>Este recurso es opcional y almacena la ubicación de la fotografía utilizada
     * para identificar visualmente al usuario dentro de la interfaz.</p>
     */
    @Column(name = "foto_perfil")
    @Comment("URL de la foto de perfil del usuario.")
    private String fotoPerfilUrl;

    /**
     * Colección de canciones marcadas como favoritas por el usuario.
     *
     * <p>Asociación de tipo Muchos a Muchos con {@link Cancion}. La tabla de unión
     * {@code usuario_canciones_favoritas} mantiene la relación sin información adicional.
     *
     * <p>Se utiliza carga diferida (`LAZY`) para optimizar el rendimiento, evitando la
     * carga automática de la colección hasta que sea requerida explícitamente.</p>
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "usuario_canciones_favoritas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "cancion_id")
    )
    @Comment("Lista de canciones favoritas del usuario.")
    private List<Cancion> cancionesFavoritas = new LinkedList<>();

    /**
     * Colección de álbumes musicales que el usuario ha indicado que le gustan.
     *
     * <p>Asociación Muchos a Muchos con {@link Album}. La tabla intermedia
     * {@code usuario_albunes_gustados} gestiona esta relación.</p>
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_albunes_gustados",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    @Comment("Álbumes que le gustan al usuario.")
    private List<Album> albumsGustados = new LinkedList<>();

    /**
     * Colección de artistas marcados como preferidos por el usuario.
     *
     * <p>Asociación Many-to-Many con {@link Artista}. Esta relación permite modelar
     * preferencias musicales del usuario a nivel de artistas.</p>
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_artistas_gustados",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "artista_id")
    )
    @Comment("Artistas que le gustan al usuario.")
    private List<Artista> artistasGustados = new LinkedList<>();

    /**
     * Lista de usuarios que este usuario sigue activamente.
     *
     * <p>Asociación recursiva Many-to-Many sobre la entidad {@link Usuario}. La tabla
     * {@code usuario_sigue_a} modela la relación asimétrica entre un “seguidor” y un “seguido”.</p>
     *
     * <p>La relación se administra desde la perspectiva del usuario que sigue a otros,
     * evitando la recursión automática y brindando un control explícito sobre la colección.</p>
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "usuario_sigue_a",
            joinColumns = @JoinColumn(name = "seguidor_id"),
            inverseJoinColumns = @JoinColumn(name = "seguido_id")
    )
    @Comment("Lista de usuarios que este usuario sigue.")
    private List<Usuario> usuariosSeguidos = new LinkedList<>();

    // ---------------------------------------------------------------------------------------------
    // MÉTODOS DE GESTIÓN DE CANCIONES FAVORITAS
    // ---------------------------------------------------------------------------------------------

    /**
     * Agrega una canción a la lista de favoritas del usuario si no se encuentra registrada previamente.
     *
     * @param cancion canción a incluir en la colección.
     */
    public void agregarCancionFavorita(Cancion cancion) {
        if (cancion != null && !cancionesFavoritas.contains(cancion)) {
            cancionesFavoritas.add(cancion);
        }
    }

    /**
     * Elimina una canción de la lista de favoritas.
     *
     * @param cancion canción que se desea remover.
     */
    public void eliminarCancionFavorita(Cancion cancion) {
        cancionesFavoritas.remove(cancion);
    }

    /**
     * Retorna una copia inmutable de la colección de canciones favoritas para evitar
     * modificaciones externas directas sobre la entidad persistida.
     *
     * @return copia defensiva de la lista de canciones favoritas.
     */
    public List<Cancion> getListaCancionesFavoritas() {
        return new LinkedList<>(cancionesFavoritas);
    }

    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DE ÁLBUMES GUSTADOS
    // ---------------------------------------------------------------------------------------------

    /**
     * Registra un álbum como gustado por el usuario.
     *
     * @param album álbum que se desea agregar.
     */
    public void agregarAlbumGustado(Album album) {
        if (album != null && !albumsGustados.contains(album)) {
            albumsGustados.add(album);
        }
    }

    /**
     * Elimina un álbum de la lista de álbumes gustados.
     *
     * @param album álbum a retirar.
     */
    public void eliminarAlbumGustado(Album album) {
        albumsGustados.remove(album);
    }

    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DE ARTISTAS PREFERIDOS
    // ---------------------------------------------------------------------------------------------

    /**
     * Registra un artista como preferido del usuario.
     *
     * @param artista artista a incluir.
     */
    public void agregarArtistaGustado(Artista artista) {
        if (artista != null && !artistasGustados.contains(artista)) {
            artistasGustados.add(artista);
        }
    }

    /**
     * Remueve un artista de la lista de preferidos.
     *
     * @param artista artista a eliminar.
     */
    public void eliminarArtistaGustado(Artista artista) {
        artistasGustados.remove(artista);
    }

    // ---------------------------------------------------------------------------------------------
    // GESTIÓN DE USUARIOS SEGUIDOS
    // ---------------------------------------------------------------------------------------------

    /**
     * Permite que este usuario siga a otro usuario del sistema.
     *
     * <p>El método impide que un usuario se siga a sí mismo y evita duplicados en la colección.</p>
     *
     * @param usuario usuario al que se desea seguir.
     */
    public void seguirUsuario(Usuario usuario) {
        if (usuario != null && !usuario.equals(this) && !usuariosSeguidos.contains(usuario)) {
            usuariosSeguidos.add(usuario);
        }
    }

    /**
     * Deja de seguir a un usuario que anteriormente era seguido.
     *
     * @param usuario usuario a dejar de seguir.
     */
    public void dejarDeSeguirUsuario(Usuario usuario) {
        usuariosSeguidos.remove(usuario);
    }

    /**
     * Retorna una copia defensiva de la lista actual de usuarios seguidos.
     *
     * @return copia de la colección de usuarios seguidos.
     */
    public List<Usuario> getListaUsuariosSeguidos() {
        return new LinkedList<>(usuariosSeguidos);
    }

    /**
     * Retorna el total de usuarios que este usuario sigue.
     *
     * @return cantidad de usuarios seguidos.
     */
    public int contarUsuariosSeguidos() {
        return usuariosSeguidos.size();
    }
}
