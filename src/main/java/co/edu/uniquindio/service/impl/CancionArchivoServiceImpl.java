package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.CancionArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancionArchivoServiceImpl implements CancionArchivoService {

    // Repositorios y utilidades inyectada vía Lombok's @RequiredArgsConstructor
    private final CancionRepo cancionRepo;
    private final UsuarioRepo usuarioRepo;



    /**
     * Genera un CSV con las canciones favoritas del usuario identificado por usuarioId.
     *
     * @param usuarioId id del usuario
     * @return ByteArrayInputStream con el contenido del CSV
     * @throws ElementoNoEncontradoException si el usuario no existe
     * @throws Exception en caso de errores de entrada/salida
     */
    @Override
    public ByteArrayInputStream generarReporteFavoritos(Long usuarioId) throws ElementoNoEncontradoException, Exception {

        // Buscar el usuario por su ID en la base de datos; si no existe, lanzar excepción.
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new ElementoNoEncontradoException("Usuario no encontrado con id: " + usuarioId));

        // Obtener la lista de canciones favoritas del usuario.
        List<Cancion> favoritas = usuario.getListaCancionesFavoritas();

        // Crear un buffer en memoria para generar el CSV (salida binaria).
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Envolver el flujo binario en un escritor de texto para poder escribir líneas.
        // Usamos UTF-8 para soportar caracteres acentuados en los títulos/artistas.
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, "UTF-8"))) {

            // Escribir la primera línea con los encabezados del CSV.
            writer.write("ID;Titulo;Genero;FechaLanzamiento;Artista;Duracion");
            writer.newLine(); // Salto de línea después de los encabezados.

            // Iterar por cada canción favorita y escribir sus campos separados por ';'.
            for (Cancion c : favoritas) {
                // Preparar cada campo (evitar nulos)
                String id = c.getId() == null ? "" : c.getId().toString();
                String titulo = c.getTitulo() == null ? "" : c.getTitulo();
                String genero = c.getGeneroMusical() == null ? "" : c.getGeneroMusical().toString();
                String fecha = c.getFechaLanzamiento() == null ? "" : c.getFechaLanzamiento().toString();
                String artista = (c.getArtistaPrincipal() == null || c.getArtistaPrincipal().getNombreArtistico() == null)
                        ? "" : c.getArtistaPrincipal().getNombreArtistico();
                String duracion = c.getDuracion() == null ? "" : c.getDuracion();

                // Escribir la línea formateada con delimitador ';'
                writer.write(String.join(";", id, titulo, genero, fecha, artista, duracion));
                writer.newLine(); // Nueva línea para la siguiente canción.
            }

            // Forzar el vaciado del buffer al flujo subyacente.
            writer.flush();
        } catch (IOException e) {
            // Si ocurre un error de E/S, envolver la excepción y re-lanzarla.
            throw new Exception("Error al generar el CSV: " + e.getMessage(), e);
        }

        // Crear un ByteArrayInputStream a partir de los bytes escritos en memoria
        // y retornarlo para que el controlador pueda leer los bytes y enviarlos en la respuesta.
        return new ByteArrayInputStream(baos.toByteArray());
    }



    /**
     * Genera un reporte general de todas las canciones registradas en el sistema en formato de texto plano (TXT).
     *
     * <p>Recupera todas las canciones de la base de datos y formatea sus metadatos clave
     * en un {@code ByteArrayInputStream} para su descarga como archivo.</p>
     *
     * @return Un {@code ByteArrayInputStream} que contiene el contenido del reporte TXT codificado en UTF-8.
     * @throws Exception Si ocurre un error durante la obtención de datos, la manipulación de flujos o la escritura del archivo.
     */
    @Override
    public ByteArrayInputStream generarReporteGeneralCanciones() throws Exception {

        // Obtener todas las canciones del repositorio de la base de datos.
        List<Cancion> canciones = cancionRepo.findAll();

        // Crear un flujo de salida en memoria (ByteArrayOutputStream) para almacenar temporalmente el contenido del reporte.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Inicializar un BufferedWriter para escribir texto de manera eficiente,
        // envolviendo el flujo de bytes en un OutputStreamWriter para asegurar la codificación UTF-8.
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, "UTF-8"))) {

            // Encabezado del reporte
            writer.write("==== REPORTE GENERAL DE CANCIONES ====");
            writer.newLine();
            writer.write("Total de canciones: " + canciones.size());
            writer.newLine();
            writer.newLine();

            // Escribir la información detallada de cada canción
            for (Cancion c : canciones) {
                writer.write("ID: " + (c.getId() != null ? c.getId() : "N/A"));
                writer.newLine();
                writer.write("Título: " + (c.getTitulo() != null ? c.getTitulo() : "N/A"));
                writer.newLine();
                writer.write("Género: " + (c.getGeneroMusical() != null ? c.getGeneroMusical().toString() : "N/A"));
                writer.newLine();
                writer.write("Fecha de lanzamiento: " + (c.getFechaLanzamiento() != null ? c.getFechaLanzamiento().toString() : "N/A"));
                writer.newLine();
                writer.write("Artista: " +
                        (c.getArtistaPrincipal() != null && c.getArtistaPrincipal().getNombreArtistico() != null
                                ? c.getArtistaPrincipal().getNombreArtistico() : "N/A"));
                writer.newLine();
                writer.write("Duración: " + (c.getDuracion() != null ? c.getDuracion() : "N/A"));
                writer.newLine();
                writer.write("----------------------------------------");
                writer.newLine();
            }

            // Asegurar que todos los datos en el buffer se escriban al ByteArrayOutputStream.
            writer.flush();

        } catch (IOException e) {
            // Capturar errores de I/O y relanzarlos como una excepción general con el mensaje.
            throw new Exception("Error al generar el archivo TXT: " + e.getMessage(), e);
        }

        // Convertir el contenido a flujo de entrada (para descarga)
        return new ByteArrayInputStream(baos.toByteArray());
    }


}




