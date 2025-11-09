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
 * Entidad que representa a un Artista de las Canciones registradas.
 *
 * <p>Esta entidad es fundamental para la organización del contenido, ya que establece la
 * relación uno a muchos con la entidad {@link Cancion} (un artista puede tener muchas canciones).
 *
 * <p>Utiliza anotaciones de Lombok para generar automáticamente los *getters*, *setters*,
 * y constructores.
 *
 * @see Cancion
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artistas")
@Comment("Entidad que representan a los Artistas de las canciones.")
public class Artista {

    /**
     * Identificador único del artista.
     * <p>Es la clave primaria de la tabla, generada automáticamente por la base de datos (Oracle SQL)
     * utilizando una estrategia de identidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID creado automáticamente por Oracle SQL
    @Comment("ID interno único de los artistas, generado automáticamente por el sistema.")
    private Long id;

    /**
     * Nombre artístico o de la banda.
     * <p>Es el nombre público por el cual se conoce al artista en la plataforma.
     * Es un campo obligatorio (`nullable = false`).
     */
    @Column(name = "nombre_artistico", nullable = false) // El nombre no puede ser nulo.
    @Comment("Nombre artístico del intérprete.")
    private String nombreArtistico;

    /**
     * Conjunto de canciones donde este artista figura como el intérprete principal.
     * <p>Define la relación Uno a Muchos (`@OneToMany`) con la entidad {@link Cancion}.
     * <p>El atributo `mappedBy = "artistaPrincipal"` indica que la relación es gestionada por el campo
     * `artistaPrincipal` dentro de la entidad {@link Cancion}.
     * <p>El `CascadeType.ALL` asegura que las operaciones (como la eliminación) realizadas sobre el Artista
     * se propaguen a las canciones asociadas (aunque esto debe usarse con precaución, ya que eliminar
     * un artista podría eliminar todo su catálogo).
     */
    @OneToMany(mappedBy = "artistaPrincipal", cascade = CascadeType.ALL)
    @Comment("Canciones donde el artista es el intérprete.")
    private Set<Cancion> canciones = new HashSet<>();

}

