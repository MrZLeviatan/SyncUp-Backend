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
 * Entidad que representa a un Usuario estándar de la plataforma.
 *
 * <p>Esta clase extiende de la clase abstracta {@link Persona}, heredando atributos de identificación y
 * autenticación (ID, nombre, username, password).
 *
 * <p>Su principal característica es la gestión de las relaciones que definen el comportamiento del usuario,
 * como la lista de canciones favoritas.
 *
 * @see Persona
 * @see Cancion
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
     * Lista de canciones marcadas como favoritas por el usuario.
     *
     * <p>Define una relación Muchos a Muchos (`@ManyToMany`) con la entidad {@link Cancion}, ya que:
     * <ul>
     * <li>Un usuario puede tener muchas canciones favoritas.</li>
     * <li>Una canción puede ser la favorita de muchos usuarios.</li>
     * </ul>
     *
     * <p>La relación se mapea a través de la tabla intermedia (o de unión) {@code "usuario_canciones_favoritas"}.
     * Él {@code fetch = FetchType.LAZY} indica que la lista de canciones no se cargará automáticamente
     * de la base de datos a menos que sea solicitada explícitamente.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_canciones_favoritas",
            joinColumns = @JoinColumn(name = "usuario_id"),            // Clave foránea del usuario
            inverseJoinColumns = @JoinColumn(name = "cancion_id")      // Clave foránea de la canción
    )
    @Comment("Lista de canciones favoritas del usuario.")
    private List<Cancion> cancionesFavoritas = new LinkedList<>(); // Uso de LinkedList


    /**
     * Agrega una {@code Cancion} a la lista de favoritas del usuario.
     *
     * <p>Verifica que la canción no sea nula y que no esté ya presente en la lista para evitar duplicados.
     *
     * @param cancion la canción a agregar.
     */
    public void agregarCancionFavorita(Cancion cancion) {
        if (cancion != null && !cancionesFavoritas.contains(cancion)) {
            cancionesFavoritas.add(cancion);
        }
    }


    /**
     * Elimina una {@code Cancion} de la lista de favoritas del usuario.
     *
     * @param cancion la canción a eliminar.
     */
    public void eliminarCancionFavorita(Cancion cancion) {
        cancionesFavoritas.remove(cancion);
    }


    /**
     * Retorna una copia segura de la lista actual de canciones favoritas del usuario.
     *
     * <p>Devuelve una nueva {@code LinkedList} para encapsular el atributo y prevenir modificaciones
     * externas directas sobre el objeto persistido.
     *
     * @return Una copia de la lista de canciones favoritas.
     */
    public List<Cancion> getListaCancionesFavoritas() {
        return new LinkedList<>(cancionesFavoritas);
    }

}