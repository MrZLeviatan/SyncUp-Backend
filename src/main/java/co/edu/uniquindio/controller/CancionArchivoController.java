package co.edu.uniquindio.controller;

import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.service.CancionArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/canciones")
@RequiredArgsConstructor
public class CancionArchivoController {


    private final CancionArchivoService cancionArchivoService;

    /**
     * Endpoint que genera y devuelve un CSV con las canciones favoritas del usuario.
     * La respuesta HTTP es el propio archivo (el navegador inicia la descarga).
     *
     * @param usuarioId id del usuario cuyas canciones favoritas se exportarán
     * @return ResponseEntity con el CSV como bytes y cabeceras para descarga
     * @throws ElementoNoEncontradoException si el usuario no existe
     * @throws IOException en caso de error al leer los bytes del stream
     */
    @GetMapping("/reporte-favoritos/{usuarioId}")
    @PreAuthorize("hasRole('USUARIO')") // Restringe el acceso solo a usuarios con el rol 'USUARIO'.
    public ResponseEntity<byte[]> descargarReporteFavoritos(@PathVariable Long usuarioId)
            throws ElementoNoEncontradoException, IOException, Exception {

        // Llamar al servicio para generar el CSV en memoria y obtener un stream
        ByteArrayInputStream csvStream = cancionArchivoService.generarReporteFavoritos(usuarioId);

        // Leer todos los bytes del stream (contenido completo del CSV)
        byte[] csvBytes = csvStream.readAllBytes();

        // Preparar las cabeceras HTTP para indicar que la respuesta es un archivo descargable
        HttpHeaders headers = new HttpHeaders();
        // Indicar al navegador que guarde el contenido como un archivo con nombre "favoritos.csv"
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"favoritos.csv\"");
        // Indicar el tipo MIME del contenido como CSV
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        // Se puede añadir longitud de contenido (opcional)
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(csvBytes.length));

        // Devolver la respuesta con código 200 OK, cabeceras y el cuerpo binario (archivo)
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }



    /**
     * Endpoint que genera y devuelve un TXT con todas las canciones registradas en el sistema.
     *
     * <p>Recupera el reporte generado en formato {@code ByteArrayInputStream} desde la capa de servicio
     * y configura las cabeceras HTTP necesarias para forzar la descarga del archivo por parte del navegador.</p>
     *
     * @return {@code ResponseEntity<byte[]>} con el archivo TXT y cabeceras para descarga.
     * @throws Exception Si ocurre un error durante la generación del reporte o la lectura de bytes.
     */
    @GetMapping("/reporte-general")
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden descargar el reporte general.
    public ResponseEntity<byte[]> descargarReporteGeneralCanciones() throws Exception {

        // Llamar al servicio para generar el reporte TXT y obtenerlo como un flujo de entrada en memoria.
        ByteArrayInputStream txtStream = cancionArchivoService.generarReporteGeneralCanciones();

        // Convertir el flujo de entrada (InputStream) a un arreglo de bytes ([]byte) para el cuerpo de la respuesta HTTP.
        byte[] txtBytes = txtStream.readAllBytes();

        // Preparar un objeto HttpHeaders para configurar las cabeceras de la respuesta.
        HttpHeaders headers = new HttpHeaders();
        // 1. Cabecera Content-Disposition: Indica al navegador que debe descargar el contenido y sugiere el nombre del archivo.
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_canciones.txt\"");
        // 2. Cabecera Content-Type: Especifica que el cuerpo de la respuesta es texto plano con codificación UTF-8.
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
        // 3. Cabecera Content-Length: Especifica el tamaño exacto del archivo en bytes.
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(txtBytes.length));

        // Retornar la respuesta final con el archivo.
        return ResponseEntity.ok()
                .headers(headers) // Asigna las cabeceras preparadas.
                .contentType(MediaType.parseMediaType("text/plain")) // Define el tipo de contenido nuevamente para claridad.
                .body(txtBytes); // Asigna el contenido del archivo al cuerpo de la respuesta.
    }
}
