package co.edu.uniquindio.repo;

import co.edu.uniquindio.models.Artista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de persistencia para la entidad Artista (asumiendo que existe una entidad llamada Artista).
 *
 * <p>Esta interfaz extiende de {@link JpaRepository}, lo que le permite heredar automáticamente
 * un conjunto completo de métodos para la gestión de la persistencia (operaciones CRUD,
 * paginación, y ordenamiento) para la entidad {@code Artista}, cuya clave primaria es de tipo {@code Long}.
 *
 * <p>La anotación {@code @Repository} marca esta interfaz como un componente de la capa de
 * persistencia de Spring, permitiendo su inyección automática en la capa de servicios.
 *
 * <p>**Nota sobre la firma:** Se asume que la firma correcta debería ser {@code JpaRepository<Artista, Long>}
 * para trabajar con la entidad {@code Artista}.
 * @see JpaRepository
 */
@Repository
public interface ArtistaRepo extends JpaRepository<Artista, Long> {

}
