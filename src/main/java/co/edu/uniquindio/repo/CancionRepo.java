package co.edu.uniquindio.repo;


import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.enums.GeneroMusical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Repositorio de persistencia para la entidad {@link Cancion}.
 *
 * <p>Esta interfaz extiende de {@link JpaRepository}, heredando métodos CRUD básicos
 * y paginación para la entidad {@link Cancion}, cuya clave primaria es de tipo {@code Long}.
 *
 * <p>Además de las funcionalidades heredadas, define métodos de consulta personalizados
 * siguiendo las convenciones de nomenclatura de Spring Data JPA para facilitar
 * la interacción con la base de datos sin necesidad de escribir código SQL explícito.
 *
 * @see Cancion
 */
public interface CancionRepo extends JpaRepository<Cancion, Long>, JpaSpecificationExecutor<Cancion> {


    /**
     * Obtiene una lista de todas las canciones asociadas a un artista principal específico.
     *
     * <p>Esta consulta se genera automáticamente por Spring Data JPA basándose en el nombre del método:
     * {@code findBy} (Buscar por) + {@code ArtistaPrincipal} (Nombre del atributo en la clase Cancion) +
     * {@code _Id} (Clave primaria del Artista).
     *
     * @param artistaId El ID ({@code Long}) del {@link co.edu.uniquindio.models.Artista} principal.
     * @return Una {@code List} de objetos {@code Cancion} que tienen el ID de artista proporcionado.
     */
    List<Cancion> findByArtistaPrincipal_Id(Long artistaId);


    /**
     * Obtiene una lista de todas las canciones que pertenecen a un {@link GeneroMusical} específico.
     *
     * <p>La consulta se genera automáticamente basándose en el nombre del método:
     * {@code findBy} (Buscar por) + {@code GeneroMusical} (Nombre del atributo en la clase Cancion).
     *
     * @param genero El valor del {@code GeneroMusical} (enum) a buscar.
     * @return Una {@code List} de objetos {@code Cancion} con el género musical coincidente.
     */
    List<Cancion> findByGeneroMusical(GeneroMusical genero);


    /**
     * Busca canciones cuyos títulos coincidan con alguno en la lista dada (ignorando mayúsculas/minúsculas).
     *
     * @param titulos Lista de títulos coincidentes.
     * @return Lista de canciones encontradas.
     */
    List<Cancion> findByTituloInIgnoreCase(List<String> titulos);
}
