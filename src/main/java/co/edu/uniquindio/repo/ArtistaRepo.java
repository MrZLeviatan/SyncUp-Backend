package co.edu.uniquindio.repo;

import co.edu.uniquindio.models.Artista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    /**
     * Busca y retorna una lista de artistas cuyo nombre artístico coincida exactamente con el parámetro, ignorando mayúsculas y minúsculas.
     *
     * <p>Esta convención de nombre de método de Spring Data JPA se traduce a una consulta
     * SQL que utiliza el campo {@code nombreArtistico} y aplica una comparación
     * sin distinción entre mayúsculas y minúsculas.</p>
     *
     * @param nombresArtisticos El nombre artístico a buscar (ej. "SHAKIRA", "shakira").
     * @return Una lista de {@link Artista} que coinciden con el nombre artístico.
     */
    List<Artista> findByNombreArtisticoInIgnoreCase(List<String> nombresArtisticos);


    // Buscar si existe un artista con ese nombre artístico (sin distinguir mayúsculas/minúsculas)
    boolean existsByNombreArtisticoIgnoreCase(String nombreArtistico);




}
