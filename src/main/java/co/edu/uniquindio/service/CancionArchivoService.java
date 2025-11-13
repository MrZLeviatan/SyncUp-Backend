package co.edu.uniquindio.service;

import co.edu.uniquindio.exception.ElementoNoEncontradoException;

import java.io.ByteArrayInputStream;

/**
 * Interfaz de servicio que define el contrato para la generación de reportes de canciones en formatos de archivo (CSV, TXT).
 *
 * <p>Este servicio se encarga de construir el contenido de los archivos de reporte en memoria
 * para su posterior descarga a través de los controladores, desacoplando la lógica de formato
 * de la capa de presentación.</p>
 *
 */
public interface CancionArchivoService {


    /**
     * Genera un reporte de las canciones favoritas de un usuario en formato CSV en memoria.
     *
     * <p>El reporte es construido como un flujo de bytes para ser leído y descargado directamente.</p>
     *
     * @param usuarioId ID del usuario del que se generará el reporte de canciones favoritas.
     * @return Un {@code ByteArrayInputStream} que contiene el contenido completo del archivo CSV.
     * @throws ElementoNoEncontradoException Si el usuario con el ID especificado no se encuentra en el sistema.
     * @throws Exception Para errores de Entrada/Salida (E/S) durante la construcción del archivo o cualquier otro error inesperado.
     */
    ByteArrayInputStream generarReporteFavoritos(Long usuarioId) throws ElementoNoEncontradoException, Exception;


    /**
     * Genera un reporte general de todas las canciones registradas en el sistema en formato de texto plano (TXT).
     *
     * <p>Recupera todas las canciones de la base de datos y formatea sus metadatos clave para su exportación.</p>
     *
     * @return Un {@code ByteArrayInputStream} que contiene el contenido completo del archivo TXT.
     * @throws Exception Para errores de Entrada/Salida (E/S) durante la construcción del archivo o cualquier otro error inesperado.
     */
    ByteArrayInputStream generarReporteGeneralCanciones() throws Exception;

}
