package co.edu.uniquindio.dto;

import co.edu.uniquindio.dto.cancion.CancionDto;

import java.util.List;

/**
 * Objeto de Transferencia de Datos (DTO) que representa una "Radio" o cola de reproducción dinámica
 * iniciada a partir de una canción base.
 *
 * <p>Este DTO es crucial para la funcionalidad de recomendación en tiempo real.
 * Contiene la referencia de la canción que inició el stream y la secuencia de canciones recomendadas
 * que deben reproducirse a continuación.
 *
 * @param idCancionBase El ID de la {@link co.edu.uniquindio.models.Cancion} que se usó como punto de partida
 * para generar la cola de reproducción de la radio.
 * @param colaReproduccion La lista ordenada de {@link CancionDto} que sigue a la canción base.
 *
 * @see CancionDto
 */
public record RadioDto(

        Long idCancionBase,
        List<CancionDto> colaReproduccion

) {
}