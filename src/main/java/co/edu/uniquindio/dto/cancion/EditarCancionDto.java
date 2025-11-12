package co.edu.uniquindio.dto.cancion;

import co.edu.uniquindio.models.enums.GeneroMusical;

import java.time.LocalDate;


/**
 * Objeto de Transferencia de Datos (DTO) de entrada utilizado para solicitar la edición de los metadatos de una canción existente.
 *
 * <p>Utiliza un Record de Java, garantizando inmutabilidad. Este DTO incluye el identificador
 * de la canción y los campos que pueden ser modificados por el artista o administrador.
 *
 * <p>Los campos que representan archivos binarios (pista de audio, portada) se omiten, ya que
 * la edición de metadatos es una operación separada de la subida o reemplazo de archivos.
 *
 * @param id El identificador único de la canción que será editada (obligatorio).
 * @param titulo El nuevo título opcional de la canción.
 * @param fechaLanzamiento La nueva fecha de lanzamiento opcional ({@code LocalDate}).
 *
 * @see GeneroMusical
 */
public record EditarCancionDto(


        Long id,
        String titulo,
        LocalDate fechaLanzamiento
) {
}
