package co.edu.uniquindio.dto.artista;

import java.util.Set;

/**
 * Record DTO (Data Transfer Object) utilizado para la transferencia de información básica de un Artista.
 *
 * <p>Este DTO se usa comúnmente para listar artistas o para referencias sencillas dentro de otros objetos,
 * minimizando la información transferida.</p>
 *
 * @param id Identificador único del artista.
 * @param nombreArtistico Nombre o pseudónimo artístico del artista.
 *
 */
public record ArtistaDto(

        Long id,
        String nombreArtistico,
        String urlImagen,
        String descripcion,
        int seguidores,
        Set<String> miembros

) {
}
