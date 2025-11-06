package co.edu.uniquindio.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@MappedSuperclass
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID creado automáticamente por Oracle SQL
    @Comment("ID interno único de la persona creado automáticamente por el sistema.")
    private Long id;

    @Column(name = "nombre_completo", nullable = false) // El nombre no puede ser nulo.
    @Comment("Nombre completo del usuario (nombre y apellidos).")
    private String nombre;

    @Column(name = "username", nullable = false) // Cada username no puede ser nulo
    @Comment("Username único de la Persona.")
    private String username;

    @Column(name = "password", nullable = false)  // El password no puede ser nulo
    @Comment("Contraseña cifrada del usuario para autenticación.")
    private String password;


    // ----------- equals() y hashCode() basado en 'username' -----------

    /**
     * Este método determina si dos objetos Persona son iguales.
     *  La comparación se basa únicamente en el campo 'username', ya que este es único para cada objeto Persona dentro del sistema.
     */
    @Override
    public boolean equals(Object o) {
        // Si es el mismo objeto en memoria
        if (this == o) return true;

        // Si el objeto a comparar es nulo o de otra clase, no son iguales.
        if (o == null || getClass() != o.getClass()) return false;

        Persona persona = (Persona) o;

        // Se comparan los usernames (únicos) de ambas Personas.
        return Objects.equals(username, persona.username);
    }


    /**
     * Calcula el código hash del objeto basado en el campo 'username'.
     *  Esto garantiza coherencia con el método equals().
     */
    @Override
    public int hashCode() {
        // Usa el campo 'username' para generar el hash.
        return Objects.hash(username);
    }

}
