package co.edu.uniquindio.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;


import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
@Comment("Entidad que representa a los usuarios registrados del sistema.")
public class Usuario extends Persona {


    // Lista de canciones Favoritas (Muchas a Muchas - Una canción puede ser la favorita de muchos Usuarios)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_canciones_favoritas",
            joinColumns = @JoinColumn(name = "usuario_id"),            // Clave foránea del usuario
            inverseJoinColumns = @JoinColumn(name = "cancion_id")      // Clave foránea de la canción
    )
    @Comment("Lista de canciones favoritas del usuario (almacenadas en una LinkedList).")
    private List<Cancion> cancionesFavoritas = new LinkedList<>(); // Uso de LinkedList


    /**
     * Agrega una canción a la lista de favoritas del usuario.
     *
     * @param cancion la canción a agregar
     */
    public void agregarCancionFavorita(Cancion cancion) {
        if (cancion != null && !cancionesFavoritas.contains(cancion)) {
            cancionesFavoritas.add(cancion);
        }
    }


    /**
     * Elimina una canción de la lista de favoritas del usuario.
     *
     * @param cancion la canción a eliminar
     */
    public void eliminarCancionFavorita(Cancion cancion) {
        cancionesFavoritas.remove(cancion);
    }


    /**
     * Retorna la lista actual de canciones favoritas del usuario.
     *
     * @return lista de canciones favoritas
     */
    public List<Cancion> getListaCancionesFavoritas() {
        return new LinkedList<>(cancionesFavoritas);
    }

}
