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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cancion")
@Comment("Entidad que representan las Canciones registradas.")
public class Cancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID creado automáticamente por Oracle SQL
    @Comment("ID interno único de la canción, generado automáticamente por el sistema.")
    private Long id;

    @Column(name = "titulo_cancion", nullable = false) // El título no puede ser nulo.
    @Comment("Titulo de la canción")
    private String titulo;


    @Enumerated(EnumType.STRING)    // Convertir el enum String para el guardado
    @Column(name = "genero_musical", nullable = false)
    @Comment("Genero musical de la canción")
    private GeneroMusical generoMusical;

    @Column(name = "fecha_lanzamiento", nullable = false)   // Fecha de lanzamiento de la canción
    @Comment("Fecha de lanzamiento de la canción.")
    private LocalDate fechaLanzamiento;


    @Column(name = "imagen_portada_url")
    @Comment("URL de la imagen de la portada (Almacenada en Cloudinary).")
    private String urlPortada;


    @Column(name = "cancion_url")
    @Comment("URL de la canción MP3 (Almacenada en Cloudinary).")
    private String urlCancion;

    // Se calcula automáticamente la duración con la librería Map(Algo)
    @Column(name = "duración_canción")
    @Comment("Duración de la canción en mm:ss")
    private String duracion;


    // Relación Muchos a Uno con el artista (Una canción es de un solo artista)
    @ManyToOne(optional = false) // Es obligatorio que una canción tenga un artista principal
    @JoinColumn(name = "artista_principal_id", nullable = false)
    @Comment("Artista principal de la canción.")
    private Artista artistaPrincipal;


    // ----------- equals() y hashCode() basado en 'id' -----------

    // Dos canciones son iguales si comparten el mismo ID en la base de datos.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                // Misma referencia
        if (o == null || getClass() != o.getClass()) return false;  // Verificación de tipo
        Cancion cancion = (Cancion) o;
        return Objects.equals(id, cancion.id);     // Comparar IDs
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);  // Usar solo el ID para el hash
    }

}
