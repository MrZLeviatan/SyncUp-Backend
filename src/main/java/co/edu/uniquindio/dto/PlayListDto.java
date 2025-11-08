package co.edu.uniquindio.dto;

import co.edu.uniquindio.dto.cancion.CancionDto;

import java.util.List;

/**
 * Objeto de Transferencia de Datos (DTO) que representa una lista de reproducción (Playlist) generada o definida.
 *
 * <p>Este DTO está diseñado para encapsular el nombre de la playlist y el conjunto de canciones
 * que la componen, utilizando la representación optimizada {@link CancionDto}.
 *
 * @param nombre El nombre asignado a la lista de reproducción (ej. "Descubrimiento Semanal").
 * @param canciones La lista de {@link CancionDto} que componen la playlist.
 *
 * @see CancionDto
 */
public record PlayListDto(

        String nombre,
        List<CancionDto> canciones

) {
}