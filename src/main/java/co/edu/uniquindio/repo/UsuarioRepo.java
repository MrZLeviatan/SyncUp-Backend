package co.edu.uniquindio.repo;

import co.edu.uniquindio.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para la entidad {@link Usuario}.
 *
 * <p>Esta interfaz extiende de {@link JpaRepository}, lo que le permite heredar automáticamente
 * un conjunto completo de métodos para la gestión de la persistencia (operaciones CRUD,
 * paginación, y ordenamiento) para la entidad {@link Usuario}, cuya clave primaria es de tipo {@code Long}.
 *
 * <p>La anotación {@code @Repository} marca esta interfaz como un componente de la capa de
 * persistencia de Spring, permitiendo que sea inyectada automáticamente en la capa de servicios.
 *
 * @see Usuario
 * @see JpaRepository
 */
@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {


    Optional<Usuario> findByUsername(String username);


    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.usuariosSeguidos")
    List<Usuario> findAllConUsuariosSeguidos();

}
