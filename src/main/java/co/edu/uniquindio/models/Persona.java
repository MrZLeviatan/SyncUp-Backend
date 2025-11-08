package co.edu.uniquindio.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.Objects;

/**
 * Clase Abstracta Base que contiene los atributos y métodos comunes a todas las personas/usuarios del sistema.
 *
 * <p>Esta clase está anotada con {@code @MappedSuperclass}, lo que significa que sus atributos de
 * mapeo (ID, nombre, username, password) se incluirán directamente en las tablas de las clases que la hereden
 * (por ejemplo, {@link Usuario} y {@link Admin}), pero no se mapeará a una tabla propia en la base de datos.
 *
 * <p>Define las propiedades esenciales para la autenticación y la identificación.
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class Persona {

    /**
     * Identificador único de la persona.
     * <p>Es la clave primaria en la tabla de la entidad concreta que herede esta clase.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID creado automáticamente por Oracle SQL
    @Comment("ID interno único de la persona creado automáticamente por el sistema.")
    private Long id;

    /**
     * Nombre completo de la persona (nombre y apellidos).
     * <p>Campo obligatorio (`nullable = false`).
     */
    @Column(name = "nombre_completo", nullable = false) // El nombre no puede ser nulo.
    @Comment("Nombre completo del usuario (nombre y apellidos).")
    private String nombre;

    /**
     * Nombre de usuario (Username) único para la autenticación.
     * <p>Campo obligatorio (`nullable = false`) y debe ser único en la base de datos (`unique = true`).
     */
    @Column(name = "username", nullable = false, unique = true) // Cada username debe ser único y no puede ser nulo
    @Comment("Username único de la Persona.")
    private String username;

    /**
     * Contraseña cifrada para la autenticación del usuario.
     * <p>Campo obligatorio (`nullable = false`). La contraseña debe almacenarse cifrada (hash) por seguridad.
     */
    @Column(name = "password", nullable = false)  // El password no puede ser nulo
    @Comment("Contraseña cifrada del usuario para autenticación.")
    private String password;


    // -------------------------------------------------------------------------------------------------
    // Métodos `equals()` y `hashCode()`
    // -------------------------------------------------------------------------------------------------

    /**
     * Compara si esta instancia de {@code Persona} es igual a otro objeto.
     * <p>La igualdad se basa exclusivamente en la unicidad del username, ya que este es un identificador
     * de negocio clave y es único en el sistema.
     *
     * @param o El objeto con el que se va a comparar.
     * @return {@code true} si los usernames son iguales; {@code false} en caso contrario.
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
     * <p>Esto garantiza coherencia con el método {@code equals()}, cumpliendo con el contrato de Java.
     *
     * @return El valor del código hash basado en el username.
     */
    @Override
    public int hashCode() {
        // Usa el campo 'username' para generar el hash.
        return Objects.hash(username);
    }

}