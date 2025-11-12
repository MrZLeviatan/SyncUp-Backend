package co.edu.uniquindio.service.impl;

import co.edu.uniquindio.dto.PlayListDto;
import co.edu.uniquindio.dto.RadioDto;
import co.edu.uniquindio.dto.cancion.CancionDto;
import co.edu.uniquindio.exception.ElementoNoEncontradoException;
import co.edu.uniquindio.graph.GrafoDeSimilitud;
import co.edu.uniquindio.mapper.CancionMapper;
import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.repo.CancionRepo;
import co.edu.uniquindio.service.RecomendacionService;
import co.edu.uniquindio.service.utils.SimilitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación concreta de la interfaz {@link RecomendacionService}.
 *
 * <p>Contiene la lógica de negocio para generar colas de radio y listas de reproducción
 * personalizadas, utilizando el {@link GrafoDeSimilitud} pre-construido en {@link SimilitudService}.
 *
 * <p>Utiliza Spring Data JPA ({@link CancionRepo}) para la búsqueda inicial de entidades y MapStruct
 * ({@link CancionMapper}) para la transformación de entidades a DTOs.
 *
 * @see RecomendacionService
 * @see SimilitudService
 */
@Service
@RequiredArgsConstructor
public class RecomendacionServiceImpl implements RecomendacionService {

    // Servicio que proporciona acceso al grafo de similitud precálculo
    private final SimilitudService similitudService;

    /** Repositorio para buscar entidades {@link Cancion} en la base de datos. */
    private final CancionRepo cancionRepo;

    /** Mapper para convertir entidades {@link Cancion} a DTOs {@link CancionDto}. */
    private final CancionMapper cancionMapper;

    /**
     * Inicia y genera una cola de reproducción tipo "Radio" a partir de una canción base.**
     *
     * <p>La radio se genera extrayendo los 10 vecinos más similares de la canción base
     * directamente desde él {@link GrafoDeSimilitud}.
     *
     * @param cancionId El ID de la canción que sirve como punto de partida.
     * @return Un objeto {@link RadioDto} que contiene la cola de reproducción ordenada por similitud.
     * @throws ElementoNoEncontradoException Si la canción base no se encuentra en la base de datos.
     */
    @Override
    public RadioDto iniciarRadio(Long cancionId) throws ElementoNoEncontradoException {

        // 1. Obtiene el grafo
        GrafoDeSimilitud grafo = similitudService.obtenerGrafo();

        // Busca la canción base en la base de datos usando su ID.
        Cancion cancionBase = cancionRepo.findById(cancionId)
                // Si no se encuentra, lanza una exceptión.
                .orElseThrow(()-> new ElementoNoEncontradoException("Canción con ID:" + cancionId + "No encontrada"));

        // 2. Obtiene los vecinos directos y los ordena por peso (costo).
        Map<Cancion, Double> vecinos = grafo.obtenerVecinos(cancionBase);

        // Transforma los vecinos del mapa en una lista ordenada de canciones similares.
        List<CancionDto> similares = vecinos.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()) // Ordenar: Menor peso (costo) = Mayor similitud
                .limit(10)                           // Seleccionar las 10 mejores
                .map(Map.Entry::getKey)              // Extraer la Cancion
                .map(cancionMapper::toDto)           // Convierte cada Cancion a CancionDto usando el mapper
                .collect(Collectors.toList());

        // Crea un objeto RadioDto que contiene la canción base y las canciones similares
        return new RadioDto(cancionBase.getId(), similares);
    }


    /**
     * Genera una lista de descubrimiento semanal basada en las canciones favoritas del {@link Usuario}.
     *
     * <p>El algoritmo funciona iterando sobre cada canción favorita del usuario y extrayendo los
     * 3 vecinos más similares a esa canción desde el grafo. Los resultados se acumulan en un
     * {@code Set} para asegurar que no haya canciones duplicadas en la recomendación final.
     *
     * @param usuario El objeto {@link Usuario} para el cual se generará la playlist.
     * @return Un objeto {@link PlayListDto} con las canciones recomendadas.
     */
    @Override
    public PlayListDto generarDescubrimientoSemanal(Usuario usuario) {

        // Obtiene el grafo de similitud actual
        GrafoDeSimilitud grafo = similitudService.obtenerGrafo();

        // Utiliza un Set para almacenar recomendaciones y evitar duplicados automáticamente.
        Set<Cancion> recomendadas = new HashSet<>();

        // Itera sobre cada canción favorita para encontrar sus "vecinos".
        for (Cancion favorita : usuario.getCancionesFavoritas()) {

            // Obtiene las canciones similares (vecinos) desde el grafo
            Map<Cancion, Double> vecinos = grafo.obtenerVecinos(favorita);

            // Obtiene los 3 vecinos más similares para esta favorita.
            vecinos.entrySet().stream()
                    // Ordena los vecinos por su peso (menor peso = más parecido)
                    .sorted(Map.Entry.comparingByValue())
                    // Limita la cantidad de canciones similares a 3 por favorita
                    .limit(3)
                    // Obtiene solo la canción (sin el peso)
                    .map(Map.Entry::getKey)
                    .forEach(recomendadas::add); // Añade al Set.
        }

        // Convierte el conjunto de canciones recomendadas a DTOs.
        List<CancionDto> cancionesDTO = recomendadas.stream()
                // Se filtra para asegurar que el usuario no vea sus propias favoritas como "descubrimiento"
                .filter(c -> !usuario.getCancionesFavoritas().contains(c))
                .map(cancionMapper::toDto)
                .collect(Collectors.toList());

        // Retorna el DTO de la playlist.
        return new PlayListDto("Descubrimiento Semanal", cancionesDTO);
    }
}
