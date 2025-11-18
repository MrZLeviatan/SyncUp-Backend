package co.edu.uniquindio.models;

import co.edu.uniquindio.exception.ElementoNoValidoException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa un álbum musical registrado en el sistema.
 *
 * <p>Esta entidad almacena la información principal de un álbum publicado por
 * un artista, incluyendo su título, fecha de lanzamiento, duración total
 * (calculada con base en la suma de las duraciones de sus canciones) y el
 * conjunto de canciones que lo componen.</p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "albums_musicales")
@Comment("Entidad que representa los Albums musicales registrados en el sistema.")
public class Album {

    /**
     * Identificador único del álbum.
     *
     * <p>Clave primaria generada automáticamente por el sistema para garantizar
     * la unicidad del registro.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("ID interno único del álbum, generado automáticamente por el sistema.")
    private Long id;

    /**
     * Título oficial del álbum.
     *
     * <p>Corresponde al nombre asignado al álbum por el artista o la casa
     * discográfica. Es un campo obligatorio dentro del modelo.</p>
     */
    @Column(nullable = false)
    @Comment("Título musical del álbum.")
    private String titulo;

    /**
     * URL de la imagen de portada del álbum.
     *
     * <p>Representa la ubicación de la imagen asociada al álbum dentro del
     * sistema o en un servicio de almacenamiento externo.</p>
     */
    @Column(nullable = false)
    @Comment("Dirección de la imagen de portada del álbum.")
    private String urlPortada;

    /**
     * Fecha oficial de lanzamiento del álbum.
     *
     * <p>Indica el día en el que el álbum fue publicado de manera formal. Es
     * un dato obligatorio que permite ordenar y consultar álbumes por su
     * fecha de salida.</p>
     */
    @Column(name = "fecha_lanzamiento", nullable = false)
    @Comment("Fecha de lanzamiento del álbum.")
    private LocalDate fechaLanzamiento;

    /**
     * Duración total del álbum expresada en formato mm:ss.
     *
     * <p>Este valor no se ingresa manualmente, sino que es calculado a partir
     * de la suma de las duraciones individuales de todas las canciones que
     * componen el álbum.</p>
     */
    @Column(name = "duracion_total")
    @Comment("Duración total del álbum, calculada automáticamente.")
    private String duracionTotal;


    // --------------------------------------------------------------------
    // Relaciones
    // --------------------------------------------------------------------


    /**
     * Artista al cual pertenece el álbum.
     *
     * <p>Relaciona el álbum con su creador. Un artista puede tener múltiples
     * álbumes, mientras que cada álbum pertenece únicamente a un artista.</p>
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "artista_id", nullable = false)
    @Comment("Artista dueño del álbum.")
    private Artista artista;

    /**
     * Conjunto de canciones incluidas en el álbum.
     *
     * <p>La relación es de uno a muchos: un álbum contiene múltiples canciones,
     * y cada canción pertenece exclusivamente a un único álbum. Se emplea
     * orphanRemoval para garantizar consistencia al eliminar canciones.</p>
     */
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Canciones pertenecientes al álbum.")
    private Set<Cancion> canciones = new HashSet<>();


    // --------------------------------------------------------------------
    // Métodos utilitarios
    // --------------------------------------------------------------------

    /**
     * Agrega una canción al álbum y establece la relación bidireccional.
     *
     * <p>Si la canción no tiene establecido este álbum como su referencia
     * principal, se actualiza automáticamente para mantener la integridad
     * de la relación.</p>
     *
     * @param cancion Canción que será agregada al álbum.
     * @throws ElementoNoValidoException si la canción proporcionada es nula.
     */
    public void agregarCancion(Cancion cancion) throws ElementoNoValidoException {
        if (cancion == null) {
            throw new ElementoNoValidoException("La canción no puede ser nula.");
        }

        canciones.add(cancion);

        if (cancion.getAlbum() != this) {
            cancion.setAlbum(this);
        }
    }

    /**
     * Elimina una canción del álbum y limpia la referencia inversa.
     *
     * <p>Si la canción pertenece al álbum, se elimina del conjunto y se
     * actualiza su referencia de álbum a {@code null} para evitar
     * inconsistencias en la relación.</p>
     *
     * @param cancion Canción que será eliminada del álbum.
     */
    public void eliminarCancion(Cancion cancion) {
        if (cancion == null || !canciones.contains(cancion)) {
            return;
        }

        canciones.remove(cancion);

        if (cancion.getAlbum() == this) {
            cancion.setAlbum(null);
        }
    }

}
