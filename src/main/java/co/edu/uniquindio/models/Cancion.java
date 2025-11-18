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
 * Representa una canción dentro del catálogo musical administrado por la plataforma.
 *
 * <p>Una canción almacena información descriptiva, metadatos técnicos y las asociaciones
 * necesarias para su correcta organización: el artista principal responsable de su producción
 * y el álbum en el que se distribuye. La entidad también integra referencias a los recursos
 * multimedia utilizados para su reproducción y visualización.</p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "canción")
@Comment("Entidad que modela el registro detallado de una canción.")
public class Cancion {

    /**
     * Identificador único y autogenerado para la canción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Clave primaria de la canción.")
    private Long id;

    /**
     * Nombre oficial o título comercial asignado a la canción.
     */
    @Column(name = "titulo_cancion", nullable = false)
    @Comment("Título o denominación oficial de la canción.")
    private String titulo;

    /**
     * Género musical con el cual se clasifica la obra.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "genero_musical", nullable = false)
    @Comment("Clasificación musical basada en el género.")
    private GeneroMusical generoMusical;

    /**
     * Fecha en la que la canción fue publicada o distribuida por primera vez.
     */
    @Column(name = "fecha_lanzamiento", nullable = false)
    @Comment("Fecha oficial de lanzamiento.")
    private LocalDate fechaLanzamiento;

    /**
     * Enlace a la portada asociada a la canción o al álbum correspondiente.
     */
    @Column(name = "imagen_portada_url")
    @Comment("URL pública para la portada.")
    private String urlPortada;

    /**
     * Ubicación del archivo de audio correspondiente a la canción.
     */
    @Column(name = "cancion_url")
    @Comment("Ruta o URL del recurso de audio.")
    private String urlCancion;

    /**
     * Duración expresada en formato estándar (mm:ss). Este dato puede ser calculado
     * automáticamente a partir del archivo multimedia o cargado manualmente.
     */
    @Column(name = "duración_canción")
    @Comment("Duración total del audio.")
    private String duracion;


    // --------------------------------------------------------------------
    // Relaciones
    // --------------------------------------------------------------------

    /**
     * Artista principal responsable de la interpretación o producción del tema.
     * Un artista puede estar asociado a múltiples canciones.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "artista_principal_id", nullable = false)
    @Comment("Artista principal vinculado.")
    private Artista artistaPrincipal;

    /**
     * Álbum al cual pertenece la canción. Un álbum agrupa un conjunto de canciones
     * relacionadas por temática, fecha de producción o lanzamiento comercial.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    @Comment("Álbum que agrupa esta canción.")
    private Album album;


    // -------------------------------------------------------------------------------------------------
    // Métodos `equals()` y `hashCode()`
    // -------------------------------------------------------------------------------------------------

    /**
     * Determina la igualdad entre dos instancias de {@code Cancion} en función
     * del identificador asignado. Si el ID coincide, se considera que representan
     * el mismo registro dentro del sistema.
     *
     * @param o objeto a comparar.
     * @return true si ambos objetos comparten el mismo ID; false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                // Misma referencia
        if (o == null || getClass() != o.getClass()) return false;  // Verificación de tipo
        Cancion cancion = (Cancion) o;
        return Objects.equals(id, cancion.id);     // Comparar IDs
    }


    /**
     * Calcula el valor hash de la entidad usando únicamente el identificador.
     * Esto garantiza coherencia con el método equals() y evita colisiones
     * innecesarias en estructuras de datos.
     *
     * @return hash asociado al ID de la canción.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}