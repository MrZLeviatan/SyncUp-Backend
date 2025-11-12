package co.edu.uniquindio.dto.cancion;

import co.edu.uniquindio.models.enums.GeneroMusical;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Objeto de Transferencia de Datos (DTO) de entrada utilizada para registrar una nueva canción en el sistema.
 *
 * <p>Utiliza un Record de Java para concisión e inmutabilidad. Este DTO encapsula tanto los metadatos
 * de la canción (título, género, fecha) como los archivos binarios asociados (la pista de audio y la portada).
 *
 * @param titulo El título de la canción.
 * @param generoMusical El género musical de la canción, utilizando el enum {@link GeneroMusical}.
 * @param fechaLanzamiento La fecha en que fue lanzada la canción ({@code LocalDate}).
 * @param archivoCancion El archivo de audio de la canción, representado como un {@code MultipartFile} para la subida.
 * @param imagenPortada La imagen de la portada o miniatura de la canción, también como {@code MultipartFile}.
 * @param artistaId El identificador único del artista al que pertenece esta canción.
 * @see GeneroMusical
 */
public record RegistrarCancionDto(

        String titulo,
        GeneroMusical generoMusical,
        LocalDate fechaLanzamiento,
        MultipartFile archivoCancion,
        MultipartFile imagenPortada,
        Long artistaId
) {
}
