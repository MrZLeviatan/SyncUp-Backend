package co.edu.uniquindio.dto.cancion;

import co.edu.uniquindio.models.enums.GeneroMusical;

/**
 * Objeto de Transferencia de Datos (DTO) que representa la información pública de una canción.
 *
 * <p>Utiliza un Record de Java para garantizar la inmutabilidad y concisión.
 * Este DTO está diseñado para ser enviado desde el backend al frontend, conteniendo
 * solo la información esencial y optimizada para el consumo.
 *
 * @param id El identificador único de la canción.
 * @param titulo El título o nombre de la canción.
 * @param generoMusical El {@link GeneroMusical} de la canción.
 * @param urlCancion La URL del archivo de audio de la canción.
 * @param urlPortada La URL de la imagen de portada de la canción.
 * @param idArtista El ID del artista principal de la canción, reemplazando el objeto Artista completo
 * para aligerar la transferencia de datos.
 *
 * @see co.edu.uniquindio.models.Cancion
 */
public record CancionDto(

        Long id,
        String titulo,
        GeneroMusical generoMusical,
        String urlCancion,
        String urlPortada,
        Long idArtista

) {
}