package co.edu.uniquindio.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa a un artista dentro de la plataforma.
 *
 * <p>Esta entidad almacena la información principal de un artista o banda,
 * incluyendo su nombre artístico, biografía, número de seguidores, miembros,
 * así como las canciones y álbumes asociados a su producción musical.</p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artistas")
@Comment("Entidad que representa a los Artistas de las canciones y álbumes.")
public class Artista {

    /**
     * Identificador único del artista.
     *
     * <p>Clave primaria generada automáticamente por el sistema para asegurar
     * la unicidad del registro.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("ID interno único del artista, generado automáticamente por el sistema.")
    private Long id;

    /**
     * Nombre artístico del artista o de la banda.
     *
     * <p>Corresponde al nombre con el cual se presenta públicamente. Es un
     * campo obligatorio dentro del modelo.</p>
     */
    @Column(name = "nombre_artistico", nullable = false)
    @Comment("Nombre artístico del artista o banda.")
    private String nombreArtistico;

    /**
     * URL de la imagen del artista
     *
     * <p>Representa la ubicación de la imagen asociada al artista
     * sistema o en un servicio de almacenamiento externo.</p>
     */
    @Column(nullable = false)
    @Comment("Dirección de la imagen de portada del artista.")
    private String urlImagen;

    /**
     * Descripción o biografía del artista.
     *
     * <p>Contiene información relevante sobre su trayectoria, estilo musical,
     * integrantes o cualquier otro dato de interés. Se almacena como un CLOB
     * debido a su posible extensión.</p>
     */
    @Column(name = "descripcion", columnDefinition = "CLOB")
    @Comment("Descripción o biografía del artista.")
    private String descripcion;

    /**
     * Número total de seguidores del artista.
     *
     * <p>Representa la cantidad de usuarios que han decidido seguir al artista.
     * Por defecto inicia en cero y puede incrementarse o decrementarse mediante
     * acciones específicas.</p>
     */
    @Column(name = "seguidores", nullable = false)
    @Comment("Número total de seguidores del artista.")
    private int seguidores = 0;

    /**
     * Lista de miembros que componen la banda o proyecto musical.
     *
     * <p>Corresponde a un conjunto de nombres individuales almacenados como
     * elementos simples, sin entidad propia. Se utiliza una tabla secundaria
     * para gestionar esta colección.</p>
     */
    @ElementCollection
    @CollectionTable(
            name = "artista_miembros",
            joinColumns = @JoinColumn(name = "artista_id")
    )
    @Column(name = "miembro")
    @Comment("Lista de nombres de los miembros del artista o banda.")
    private Set<String> miembros = new HashSet<>();


    // --------------------------------------------------------------------
    // Relaciones
    // --------------------------------------------------------------------

    /**
     * Canciones donde el artista participa como intérprete principal.
     *
     * <p>Relación uno-a-muchos: un artista puede interpretar múltiples canciones,
     * mientras que cada canción tiene un único artista principal.</p>
     */
    @OneToMany(mappedBy = "artistaPrincipal", cascade = CascadeType.ALL)
    @Comment("Canciones donde el artista es el intérprete principal.")
    private Set<Cancion> canciones = new HashSet<>();


    // --------------------------------------------------------------------
    // Métodos utilitarios (Canciones)
    // --------------------------------------------------------------------

    /**
     * Agrega una canción al artista y establece la relación bidireccional.
     *
     * <p>Si la canción proporcionada no tiene asignado este artista como su
     * intérprete principal, se actualiza para mantener la coherencia en la
     * relación.</p>
     *
     * @param cancion Canción que será asociada al artista.
     * @throws IllegalArgumentException si la canción es nula.
     */
    public void agregarCancion(Cancion cancion) {
        if (cancion == null) {
            throw new IllegalArgumentException("La canción no puede ser nula.");
        }

        this.canciones.add(cancion);

        if (cancion.getArtistaPrincipal() != this) {
            cancion.setArtistaPrincipal(this);
        }
    }

    /**
     * Elimina una canción asociada al artista.
     *
     * <p>Si la canción pertenece al conjunto del artista, se elimina y se limpia
     * la referencia inversa para evitar inconsistencias.</p>
     *
     * @param cancion Canción que será eliminada del artista.
     */
    public void eliminarCancion(Cancion cancion) {
        if (cancion == null || !this.canciones.contains(cancion)) {
            return;
        }

        this.canciones.remove(cancion);

        if (cancion.getArtistaPrincipal() == this) {
            cancion.setArtistaPrincipal(null);
        }
    }


    // --------------------------------------------------------------------
    // Métodos adicionales
    // --------------------------------------------------------------------

    /**
     * Incrementa en una unidad el número de seguidores del artista.
     *
     * <p>Este método se utiliza cuando un usuario decide comenzar a seguir al
     * artista dentro de la plataforma.</p>
     */
    public void aumentarSeguidores() {
        this.seguidores++;
    }

    /**
     * Reduce en una unidad el número de seguidores del artista.
     *
     * <p>Se utiliza cuando un usuario deja de seguir al artista. No se aplican
     * validaciones adicionales, por lo que el llamado debe controlarse desde
     * capas superiores.</p>
     */
    public void disminuirSeguidores() {
        this.seguidores--;
    }

}
