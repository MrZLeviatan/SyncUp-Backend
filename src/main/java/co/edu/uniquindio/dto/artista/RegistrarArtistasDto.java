package co.edu.uniquindio.dto.artista;

/**
 * Record DTO (Data Transfer Object) utilizado para el registro inicial de un nuevo Artista en el sistema.
 *
 * <p>Contiene únicamente los datos esenciales necesarios para crear la entidad Artista.</p>
 *
 * @param nombreArtistico El nombre o pseudónimo artístico que se registrará para el nuevo artista.
 *
 */
public record RegistrarArtistasDto(

        String nombreArtistico
) {
}
