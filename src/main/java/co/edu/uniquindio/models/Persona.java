package co.edu.uniquindio.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.Objects;

/**
 * Clase base abstracta que concentra los atributos fundamentales compartidos por todas las
 * entidades que representan personas dentro del sistema.
 *
 * <p>La anotación {@code @MappedSuperclass} indica que esta clase no se materializa como
 * una tabla independiente en la base de datos. En su lugar, sus campos se integran dentro de
 * las tablas de las subclases concretas que la extienden, tales como {@code Usuario} o {@code Admin}.
 *
 * <p>Este diseño permite reutilizar propiedades esenciales asociadas a identificación,
 * autenticación y trazabilidad, evitando duplicación de código y garantizando consistencia
 * estructural en el modelo de datos.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class Persona {

    /**
     * Identificador único asignado a la persona.
     *
     * <p>Actúa como clave primaria en las entidades concretas que heredan esta clase.
     * Su valor es autogenerado por el motor de persistencia, asegurando unicidad
     * y eficiencia al momento de manejar referencias entre entidades.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Clave primaria generada automáticamente para identificar a la persona.")
    private Long id;

    /**
     * Nombre completo del individuo, incluyendo nombres y apellidos.
     *
     * <p>Este atributo constituye un dato esencial de identificación y debe estar
     * obligatoriamente presente en todas las entidades derivadas.</p>
     */
    @Column(name = "nombre_completo", nullable = false)
    @Comment("Nombre completo de la persona (incluye nombres y apellidos).")
    private String nombre;

    /**
     * Identificador único utilizado para el proceso de autenticación.
     *
     * <p>Este campo forma parte del modelo de seguridad del sistema. Su unicidad es
     * obligatoria para evitar conflictos durante el inicio de sesión y asegurar la
     * correcta asociación de credenciales.</p>
     */
    @Column(name = "username", nullable = false, unique = true)
    @Comment("Nombre de usuario único utilizado para autenticación.")
    private String username;

    /**
     * Contraseña cifrada asociada al usuario.
     *
     * <p>Nunca debe almacenarse en texto plano. El sistema debe aplicar algoritmos de
     * hashing seguros (por ejemplo, BCrypt, Argon2 o PBKDF2) antes de persistir el valor.</p>
     */
    @Column(name = "password", nullable = false)
    @Comment("Contraseña protegida mediante cifrado para garantizar seguridad.")
    private String password;


    // ---------------------------------------------------------------------------------------------
    // Métodos de igualdad y hash
    // ---------------------------------------------------------------------------------------------

    /**
     * Determina si dos instancias de {@code Persona} representan el mismo registro
     * a nivel lógico dentro del sistema.
     *
     * <p>La comparación se basa en el campo {@code username}, dado que constituye un
     * identificador de negocio único. De este modo, dos objetos se consideran equivalentes
     * si comparten el mismo nombre de usuario, independientemente de otros atributos.</p>
     *
     * @param o objeto con el que se comparará esta instancia.
     * @return {@code true} si ambos objetos poseen el mismo username; de lo contrario, {@code false}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Persona persona = (Persona) o;
        return Objects.equals(username, persona.username);
    }

    /**
     * Calcula el valor hash de la instancia utilizando el campo {@code username}.
     *
     * <p>Este método es coherente con {@link #equals(Object)}, garantizando el cumplimiento
     * del contrato definido por la API de Java para colecciones y estructuras de hashing.</p>
     *
     * @return valor hash derivado del username.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

}
