package co.edu.uniquindio.models;

import co.edu.uniquindio.models.enums.GeneroMusical;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa una Canción registrada en el sistema.
 *
 * <p>Esta clase se mapea a la tabla "cancion" en la base de datos y almacena toda la información
 * relevante sobre una pista musical, incluyendo sus metadatos y su relación con el {@link Artista} principal.
 * Está diseñada para ser utilizada en diversas funcionalidades del sistema como:
 * <ul>
 * <li>Generación de Radio relacionada con la canción.</li>
 * <li>Generación de recomendaciones basadas en la canción.</li>
 * <li>Generación de una playlist 'Semanal' o personalizada.</li>
 * </ul>
 *
 * <p>Utiliza anotaciones de Lombok para generar automáticamente los getters, setters, constructores
 * (sin argumentos y con todos los argumentos) y las anotaciones de JPA para el mapeo de la persistencia.
 *
 * @see Artista
 * @see GeneroMusical
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "canción")
@Comment("Entidad que representan las Canciones registradas.")
public class Cancion {

    /**
     * Identificador único de la canción.
     * <p>Es la clave primaria de la tabla, generada automáticamente por la base de datos (Oracle SQL)
     * utilizando una estrategia de identidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID creado automáticamente por Oracle SQL
    @Comment("ID interno único de la canción, generado automáticamente por el sistema.")
    private Long id;

    /**
     * Título o nombre de la canción.
     * <p>Es un campo obligatorio (`nullable = false`).
     */
    @Column(name = "titulo_cancion", nullable = false) // El título no puede ser nulo.
    @Comment("Titulo de la canción")
    private String titulo;

    /**
     * Género musical de la canción.
     * <p>Se almacena como una cadena de texto en la base de datos a partir del enum {@link GeneroMusical}.
     * Es un campo obligatorio (`nullable = falsé`).
     */
    @Enumerated(EnumType.STRING)    // Convertir el enum String para el guardado
    @Column(name = "genero_musical", nullable = false)
    @Comment("Genero musical de la canción")
    private GeneroMusical generoMusical;

    /**
     * Fecha de lanzamiento de la canción.
     * <p>Almacena la fecha en que la canción fue publicada. Es un campo obligatorio (`nullable = false`).
     */
    @Column(name = "fecha_lanzamiento", nullable = false)   // Fecha de lanzamiento de la canción
    @Comment("Fecha de lanzamiento de la canción.")
    private LocalDate fechaLanzamiento;


    /**
     * URL de la imagen de portada del álbum o sencillo.
     * <p>URL del servicio de almacenamiento en la nube de Cloudinary.
     */
    @Column(name = "imagen_portada_url")
    @Comment("URL de la imagen de la portada (Almacenada en Cloudinary).")
    private String urlPortada;


    /**
     * URL del archivo de audio (MP3) de la canción.
     * <p>URL del servicio de almacenamiento en la nube de Cloudinary.
     */
    @Column(name = "cancion_url")
    @Comment("URL de la canción MP3 (Almacenada en Cloudinary).")
    private String urlCancion;

    /**
     * Duración de la canción.
     * <p>Almacenada como una cadena de texto con el formato "mm:ss" (minutos:segundos).
     * Se espera que este valor se calcule automáticamente al subir el archivo.
     */
    @Column(name = "duración_canción")
    @Comment("Duración de la canción en mm:ss")
    private String duracion;


    /**
     * Artista principal de la canción.
     * <p>Define una relación de Muchos a Uno (`ManyToOne`) donde una canción está asociada obligatoriamente
     * (`optional = false`) a un solo {@link Artista}.
     * <p>La columna de la clave foránea en la tabla `cancion` se llama `artista_principal_id`.
     */
    // Relación Muchos a Uno con el artista (Una canción es de un solo artista)
    @ManyToOne(optional = false) // Es obligatorio que una canción tenga un artista principal
    @JoinColumn(name = "artista_principal_id", nullable = false)
    @Comment("Artista principal de la canción.")
    private Artista artistaPrincipal;


    // -------------------------------------------------------------------------------------------------
    // Métodos `equals()` y `hashCode()`
    // -------------------------------------------------------------------------------------------------

    /**
     * Compara si esta instancia de {@code Cancion} es igual a otro objeto.
     * <p>La igualdad se determina exclusivamente por la comparación de sus **IDs** internos.
     *
     * @param o El objeto con el que se va a comparar.
     * @return {@code true} si los IDs de las canciones son iguales; {@code false} en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                // Misma referencia
        if (o == null || getClass() != o.getClass()) return false;  // Verificación de tipo
        Cancion cancion = (Cancion) o;
        return Objects.equals(id, cancion.id);     // Comparar IDs
    }


    /**
     * Genera el valor de código hash para esta instancia de {@code Cancion}.
     * <p>El código hash se calcula únicamente a partir del **ID** interno de la canción.
     *
     * @return El valor del código hash basado en el ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);  // Usar solo el ID para el hash
    }

}