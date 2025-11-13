package co.edu.uniquindio.service;

import co.edu.uniquindio.dto.PlayListDto;
import co.edu.uniquindio.dto.RadioDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.models.Usuario;

/**
 * Contrato (Interfaz) para el servicio central de gestión y generación de Recomendaciones Musicales.
 *
 * <p>Define las operaciones de alto nivel para generar contenido dinámico y personalizado,
 * como colas de reproducción tipo "Radio" y listas de descubrimiento semanal.
 *
 * <p>Este servicio típicamente hará uso del {@link co.edu.uniquindio.graph.GrafoDeSimilitud} para obtener sus resultados.
 *
 * @see RadioDto
 * @see PlayListDto
 * @see Usuario
 */
public interface RecomendacionService {


    /**
     * Inicia y genera una cola de reproducción dinámica tipo "Radio" a partir de una canción de origen.
     *
     * <p>La implementación debe utilizar el grafo de similitud para encontrar un camino coherente
     * de canciones relacionadas con la canción base, generando una secuencia adecuada para la reproducción continua.
     *
     * @param cancionId El ID de la {@link co.edu.uniquindio.models.Cancion} que servirá como punto de partida.
     * @return Un objeto {@link RadioDto} que contiene el ID de la canción base y la lista
     * de canciones subsiguientes en la cola de reproducción.
     */
    RadioDto iniciarRadio(Long cancionId) throws ElementoNoEncontradoException;


    /**
     * Genera una lista de reproducción personalizada de "Descubrimiento Semanal" para un {@link Usuario} dado.
     *
     * <p>La implementación debe analizar las preferencias del usuario (ej. canciones favoritas, artistas)
     * y generar una {@code PlayList} con nuevas canciones que maximicen la similitud y la relevancia
     * con sus gustos actuales.
     *
     * @param idUsuario El objeto {@link Usuario} para el cual se generará la playlist.
     * @return Un objeto {@link PlayListDto} con el nombre y la lista de canciones recomendadas.
     */
    PlayListDto generarDescubrimientoSemanal(Long idUsuario) throws ElementoNoEncontradoException;

}