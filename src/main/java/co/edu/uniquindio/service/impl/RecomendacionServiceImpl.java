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
import co.edu.uniquindio.repo.UsuarioRepo;
import co.edu.uniquindio.service.RecomendacionService;
import jakarta.annotation.PostConstruct;
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
 * personalizadas, utilizando el {@link GrafoDeSimilitud}.
 *
 * <p>Utiliza Spring Data JPA ({@link CancionRepo}) para la búsqueda inicial de entidades y MapStruct
 * ({@link CancionMapper}) para la transformación de entidades a DTOs.
 *
 * @see RecomendacionService
 * @see GrafoDeSimilitud
 */
@Service
@RequiredArgsConstructor
public class RecomendacionServiceImpl implements RecomendacionService {

    // Servicio que proporciona acceso al grafo de similitud precálculo
    private GrafoDeSimilitud grafo;

    /** Repositorio para buscar entidades {@link Cancion} en la base de datos. */
    private final CancionRepo cancionRepo;

    /** Repositorio para buscar entidades {@link Usuario} en la base de datos. */
    private final UsuarioRepo usuarioRepo;

    /** Mapper para convertir entidades {@link Cancion} a DTOs {@link CancionDto}. */
    private final CancionMapper cancionMapper;


    /**
     * Inicializa el grafo de similitud después de que Spring cargue el bean.
     */
    @PostConstruct
    public void inicializarGrafo() {

        grafo = new GrafoDeSimilitud();

        List<Cancion> canciones = cancionRepo.findAll();

        // 1. Agregar todas las canciones como nodos
        canciones.forEach(grafo::agregarCancion);

        // 2. Conectar todos los pares con un peso basado en similitud
        for (Cancion c1 : canciones) {
            for (Cancion c2 : canciones) {
                if (!c1.equals(c2)) {
                    double peso = calcularPesoSimilitud(c1, c2);
                    grafo.conectarCanciones(c1, c2, peso);
                }
            }
        }
    }


    /**
     * Calcula el peso de similitud entre dos canciones.
     *
     * @param c1 primera canción
     * @param c2 segunda canción
     * @return peso (menor = más similar)
     */
    private double calcularPesoSimilitud(Cancion c1, Cancion c2) {
        double similitud = 0.0;

        if (c1.getGeneroMusical().equals(c2.getGeneroMusical())) similitud += 0.6;
        if (c1.getArtistaPrincipal().equals(c2.getArtistaPrincipal())) similitud += 0.4;

        return 1 - similitud; // Convert similarity to cost / Convierte similitud a costo

    }


    /**
     * Inicia y genera una cola de reproducción tipo "Radio" a partir de una canción base.
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

        // 1. Busca la canción base en la base de datos usando su ID.
        Cancion cancionBase = cancionRepo.findById(cancionId)
                // Si no se encuentra, lanza una exceptión.
                .orElseThrow(()-> new ElementoNoEncontradoException("Canción con ID:" + cancionId + "No encontrada"));

        // 2. Obtiene los vecinos directos y los ordena por peso (costo).
        Map<Cancion, Double> vecinos = grafo.obtenerVecinos(cancionBase);

        // Transforma los vecinos del mapa en una lista ordenada de canciones similares.
        List<CancionDto> similares = vecinos.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()) // Ordenar: Menor peso (costo) = Mayor similitud
                .limit(10)                 // Seleccionar las 10 mejores
                .map(Map.Entry::getKey)              // Extraer la Cancion
                .map(cancionMapper::toDto)           // Convierte cada Cancion a CancionDto usando el mapper
                .collect(Collectors.toList());

        // Crea un objeto RadioDto que contiene la canción base y las canciones similares
        return new RadioDto(cancionBase.getId(), similares);
    }


    /**
     * Genera una lista de descubrimiento semanal basada en las canciones favoritas del {@link Usuario}.
     *
     * <p>Utiliza Dijkstra para hallar los caminos más similares desde las favoritas hacia otras canciones.
     * Los resultados se acumulan en un
     * {@code Set} para asegurar que no haya canciones duplicadas en la recomendación final.
     *
     * @param idUsuario El objeto {@link Usuario} para el cual se generará la playlist.
     * @return Un objeto {@link PlayListDto} con las canciones recomendadas.
     */
    @Override
    public PlayListDto generarDescubrimientoSemanal(Long idUsuario) throws ElementoNoEncontradoException {

        // Se busca al usuario mediante su Id
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(()-> new ElementoNoEncontradoException("Usuario no encontrado"));

        // Se obtiene las canciones favoritas del usuario
        List<Cancion> favoritas = usuario.getCancionesFavoritas();

        // Utiliza un Set para almacenar recomendaciones y evitar duplicados automáticamente.
        Set<Cancion> recomendadas = new HashSet<>();

        // Itera sobre cada canción favorita para encontrar sus "vecinos".
        // Por cada favorita, buscar 3 canciones más cercanas usando Dijkstra
        for (Cancion favorita : favoritas) {
            // Esto permite comparar cada favorita con todas las demás canciones.
            for (Cancion otra : grafo.obtenerCanciones()) {
                // evita compararse consigo misma o no esté ya entre las favoritas del usuario.
                if (!favorita.equals(otra) && !favoritas.contains(otra)) {
                    // Se ejecuta el algoritmo de Dijkstra desde la canción favorita hasta otra canción del grafo.
                    List<Cancion> camino = grafo.dijkstra(favorita, otra);
                    // Esto significa que sí existe una ruta real entre la favorita y la otra canción.
                    if (camino.size() > 1) {
                        // Tomar la siguiente canción después de la favorita en el camino
                        Cancion siguiente = camino.get(1);
                        // Se agrega esa canción al conjunto de recomendaciones.
                        recomendadas.add(siguiente);
                    }
                }
            }
        }

        // Limitar a 15 recomendaciones máximo y evitar repetidos
        List<CancionDto> cancionesDTO = recomendadas.stream()
                .limit(15)
                .map(cancionMapper::toDto)
                .collect(Collectors.toList());

        return new PlayListDto("Descubrimiento Semanal", cancionesDTO);
    }
}
