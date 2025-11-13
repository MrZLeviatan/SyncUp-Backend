package co.edu.uniquindio.service;

import co.edu.uniquindio.exception.ElementoNoEncontradoException;

import java.io.ByteArrayInputStream;

public interface CancionArchivoService {


    /**
     * Genera un CSV en memoria con las canciones favoritas de un usuario.
     *
     * @param usuarioId id del usuario del que se generará el reporte
     * @return flujo de bytes (ByteArrayInputStream) que contiene el CSV
     * @throws ElementoNoEncontradoException si el usuario no existe
     * @throws Exception para errores de E/S u otros
     */
    ByteArrayInputStream generarReporteFavoritos(Long usuarioId) throws ElementoNoEncontradoException, Exception;


    /**
     * Genera un archivo TXT en memoria con todas las canciones registradas en el sistema.
     *
     * @return flujo de bytes (ByteArrayInputStream) que contiene el archivo TXT
     * @throws Exception para errores de E/S u otros
     */
    ByteArrayInputStream generarReporteGeneralCanciones() throws Exception;

}
