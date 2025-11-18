package co.edu.uniquindio.repo;

import co.edu.uniquindio.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de persistencia para la entidad {@link Admin} (Administrador).
 *
 * <p>Esta interfaz extiende de {@link JpaRepository}, lo que automáticamente
 * proporciona implementaciones para las operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * básicas, así como métodos para paginación y ordenamiento.</p>
 *
 * <p>La entidad administrada es {@link Admin} y su clave primaria es de tipo {@code Long}.</p>
 *
 * @see Admin
 */
@Repository
public interface AdminRepo extends JpaRepository<Admin, Long> {

    boolean existsByUsername(String username);

    Optional<Admin> findByUsername(String username);
}
