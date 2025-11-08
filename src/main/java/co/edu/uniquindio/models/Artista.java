package co.edu.uniquindio.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artistas")
@Comment("Entidad que representan a los Artistas de las canciones.")
public class Artista {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID creado automáticamente por Oracle SQL
    @Comment("ID interno único de los artistas, generado automáticamente por el sistema.")
    private Long id;

    @Column(name = "nombre_artistico", nullable = false) // El nombre no puede ser nulo.
    @Comment("Titulo de la canción")
    private String nombreArtistico;

    // Relación uno a muchos (un artista puede tener varias canciones)
    @OneToMany(mappedBy = "artistaPrincipal", cascade = CascadeType.ALL)
    @Comment("Canciones donde el artista es el intérprete.")
    private Set<Cancion> canciones = new HashSet<>();

}

