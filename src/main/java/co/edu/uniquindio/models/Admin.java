package co.edu.uniquindio.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

/**
 * Entidad que representa a un Administrador del sistema.
 *
 * <p>Esta clase extiende de {@link Persona}, heredando atributos comunes como ID, nombre, email, etc.
 * Su rol principal es la gestión central del contenido y los usuarios de la plataforma.
 *
 * <p>Las responsabilidades principales del administrador incluyen:
 * <ul>
 * <li>Creación, lectura, actualización y eliminación (CRUD) de {@link Cancion}es.</li>
 * <li>Gestión de la información de los {@link Usuario}s.</li>
 * <li>CRUD de {@link Artista}s.</li>
 * </ul>
 *
 * <p>Utiliza anotaciones de Lombok para la generación automática de *getters*, *setters* y constructores.
 *
 * @see Persona
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Admins")
@Comment("Entidad que representa a los administradores registrados del sistema.")
public class Admin extends Persona {
}
