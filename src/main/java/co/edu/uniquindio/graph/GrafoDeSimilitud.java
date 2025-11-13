package co.edu.uniquindio.graph;

import co.edu.uniquindio.models.Cancion;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Grafo ponderado no dirigido diseñado para conectar {@link Cancion}es según su nivel de similitud.
 *
 * <p>Cada nodo del grafo es una instancia de {@link Cancion}. El peso de la arista
 * entre dos canciones representa una métrica de similitud (donde un peso menor indica mayor similitud).
 *
 * <p>Esta estructura es vital para implementar funcionalidades de recomendación y búsqueda
 * de caminos de afinidad musical (ej. "de la Cancion A a la Cancion B, ¿cuál es el puente musical más lógico?").
 *
 * @see Cancion
 */
@Component
public class GrafoDeSimilitud {

    /**
     * Estructura principal del grafo: Lista de Adyacencia.
     * <p>
     * Mapea cada {@code Cancion} (nodo) a otro {@code Map} que contiene sus vecinos.
     * El {@code Map} interno asocia la {@code Cancion} vecina con el {@code Double} que representa el peso
     * de la arista (similitud/costo).
     */
    private final Map<Cancion, Map<Cancion, Double>> adjList = new HashMap<>();


    /**
     * Agrega una nueva canción (nodo) al grafo.
     * <p>Si la canción ya existe, no hace nada (debido a {@code putIfAbsent}).
     *
     * @param cancion La canción a agregar al grafo.
     */
    public void agregarCancion(Cancion cancion) {
        // Si la canción no está en el grafo, la inserta con una lista de adyacencia vacía.
        adjList.putIfAbsent(cancion, new HashMap<>());

    }


    /**
     * Elimina una canción (nodo) del grafo junto con todas sus conexiones (aristas).
     * <p>
     *
     * @param cancion La canción a eliminar del grafo. / The song to remove from the graph.
     */
    public void eliminarCancion(Cancion cancion) {
        if (!adjList.containsKey(cancion)) return; // Si no existe, no hace nada.

        // Eliminar todas las referencias a esta canción en los vecinos
        for (Cancion vecino : adjList.get(cancion).keySet()) {
            // Remueve la canción a eliminar de las listas de adyacencia de sus vecinos
            adjList.get(vecino).remove(cancion);
        }

        // Finalmente, eliminar la propia canción del mapa principal
        adjList.remove(cancion);
    }



    /**
     * Conecta dos canciones con un peso específico de similitud (arista).
     *
     * <p>Dado que el grafo es *no dirigido*, la conexión se establece en ambas direcciones
     * (de c1 a c2 y de c2 a c1) con el mismo peso.
     *
     * @param c1 La primera canción (nodo).
     * @param c2 La segunda canción (nodo).
     * @param peso El valor del peso de la arista, representando la similitud (menor peso = mayor similitud).
     */
    public void conectarCanciones(Cancion c1, Cancion c2, double peso) {
        if (c1.equals(c2)) return;   // Evita bucles (una canción no debe conectarse consigo misma).
        agregarCancion(c1);         // Asegura que la primera canción exista en el grafo.
        agregarCancion(c2);         // Asegura que la segunda canción exista en el grafo.
        adjList.get(c1).put(c2, peso);          // Añade la segunda canción como vecina de la primera con su peso.
        adjList.get(c2).put(c1, peso);          // Añade la primera canción como vecina de la segunda (porque es no dirigido).
    }


    /**
     * Calcula la ruta de menor costo (mayor similitud) entre dos canciones usando el Algoritmo de Dijkstra.
     *
     * <p>Este método encuentra el "camino más fácil" para ir de una canción a otra, minimizando
     * la suma de los pesos de las aristas (donde el peso es inversamente proporcional a la similitud).
     *
     * @param origen La canción de inicio del camino.
     * @param destino La canción final del camino.
     * @return Una {@code List<Cancion>} que representa la secuencia del camino más corto, o una lista
     * parcial si el destino es inalcanzable desde el origen.
     */
    public List<Cancion> dijkstra(Cancion origen, Cancion destino) {

        // Almacena la distancia mínima (costo) desde el origen a cada canción.
        Map<Cancion, Double> dist = new HashMap<>();

        // Guarda el nodo anterior para reconstruir el camino más corto.
        Map<Cancion, Cancion> prev = new HashMap<>();

        // Cola de prioridad que siempre extrae la canción con la menor distancia actual.
        PriorityQueue<Cancion> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        // 1. Inicializa todas las canciones con distancia máxima (infinito).
        for (Cancion c : adjList.keySet()) dist.put(c, Double.MAX_VALUE);

        // La distancia del origen a sí mismo es cero.
        dist.put(origen, 0.0);

        // Inserta el nodo origen en la cola de prioridad.
        pq.add(origen);

        // 2. Mientras existan nodos por procesar...
        while (!pq.isEmpty()) {

            // Extrae la canción con la menor distancia.
            Cancion actual = pq.poll();

            // Si llegamos al destino, podemos detenernos.
            if (actual.equals(destino)) break;

            // Itera por todos los vecinos de la canción actual.
            for (Map.Entry<Cancion, Double> vecino : adjList.get(actual).entrySet()) {

                // Calcula la nueva posible distancia (costo).
                double nuevaDist = dist.get(actual) + vecino.getValue();

                // Si la nueva distancia es menor que la actual...
                if (nuevaDist < dist.get(vecino.getKey())) {

                    // Actualiza la distancia con la menor.
                    dist.put(vecino.getKey(), nuevaDist);

                    // Establece la canción actual como predecesora del vecino.
                    prev.put(vecino.getKey(), actual);

                    // Añade el vecino a la cola de prioridad para procesarlo después.
                    pq.add(vecino.getKey());
                }
            }
        }

        // --- Reconstruir la ruta encontrada usando el mapa 'prev' ---

        // 3. Crea una lista para almacenar la ruta (camino).
        List<Cancion> ruta = new LinkedList<>();

        // Empieza desde el nodo destino.
        Cancion paso = destino;

        while (paso != null) {

            // Añade la canción actual al inicio de la lista.
            ruta.addFirst(paso);

            // Avanza a la canción anterior en el camino.
            paso = prev.get(paso);
        }

        // Devuelve la ruta completa desde el origen hasta el destino.
        return ruta;
    }


    /**
     * Devuelve el conjunto de todas las canciones (nodos) presentes en el grafo.
     *
     * @return Un {@code Set} inmutable de todas las {@code Cancion}es.
     */
    public Set<Cancion> obtenerCanciones() {

        // Devuelve el conjunto de todas las llaves (canciones) en la lista de adyacencia.
        return adjList.keySet();
    }

    /**
     * Devuelve los vecinos directos de una canción dada y el peso de las aristas que los conectan.
     *
     * @param cancion La canción de la que se buscan los vecinos.
     * @return Un {@code Map} de vecinos y sus pesos; un mapa vacío si la canción no está en el grafo.
     */
    public Map<Cancion, Double> obtenerVecinos(Cancion cancion) {

        // Si la canción existe, devuelve sus vecinos; de lo contrario, devuelve un mapa vacío.
        return adjList.getOrDefault(cancion, Collections.emptyMap());
    }
}
