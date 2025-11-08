package co.edu.uniquindio.graph;

import co.edu.uniquindio.models.Cancion;
import java.util.*;

/**
 * Grafo ponderado no dirigido que conecta canciones según su similitud.
 * Cada nodo del grafo es una instancia de la clase Cancion.
 */
public class GrafoDeSimilitud {

    // Estructura principal: Lista de adyacencia.
    // Cada Cancion se asocia con un mapa de Cancion -> peso de similitud.
    private final Map<Cancion, Map<Cancion, Double>> adjList = new HashMap<>();


    /** Agrega una canción (nodo) al grafo si aún no existe. */
    public void agregarCancion(Cancion cancion) {
        adjList.putIfAbsent(cancion, new HashMap<>()); // Si la canción no está en el grafo, la inserta con una lista de adyacencia vacía.

    }


    /**
     * Conecta dos canciones con un peso de similitud.
     * Como el grafo es no dirigido, se crea conexión en ambos sentidos.
     */
    public void conectarCanciones(Cancion c1, Cancion c2, double peso) {
        if (c1.equals(c2)) return;   // Evita bucles (una canción no debe conectarse consigo misma).
        agregarCancion(c1);         // Asegura que la primera canción exista en el grafo.
        agregarCancion(c2);         // Asegura que la segunda canción exista en el grafo.
        adjList.get(c1).put(c2, peso);          // Añade la segunda canción como vecina de la primera con su peso.
        adjList.get(c2).put(c1, peso);          // Añade la primera canción como vecina de la segunda (porque es no dirigido).
    }


    /**
     * Calcula el camino más similar entre dos canciones usando el algoritmo Dijkstra.
     * Menor costo equivale a mayor similitud.
     */
    public List<Cancion> dijkstra(Cancion origen, Cancion destino) {

        // Almacena la distancia mínima (costo) desde el origen a cada canción.
        Map<Cancion, Double> dist = new HashMap<>();

        // Guarda el nodo anterior para reconstruir el camino más corto.
        Map<Cancion, Cancion> prev = new HashMap<>();

        // Cola de prioridad que siempre extrae la canción con la menor distancia actual.
        PriorityQueue<Cancion> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        // Inicializa todas las canciones con distancia máxima (infinito).
        for (Cancion c : adjList.keySet()) dist.put(c, Double.MAX_VALUE);

        // La distancia del origen a sí mismo es cero.
        dist.put(origen, 0.0);

        // Inserta el nodo origen en la cola de prioridad.
        pq.add(origen);

        // Mientras existan nodos por procesar...
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

        // Crea una lista para almacenar la ruta (camino).
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

    // Devuelve todas las canciones (nodos) del grafo.
    public Set<Cancion> obtenerCanciones() {

        // Devuelve el conjunto de todas las llaves (canciones) en la lista de adyacencia.
        return adjList.keySet();
    }

    // Devuelve los vecinos de una canción dada.
    public Map<Cancion, Double> obtenerVecinos(Cancion cancion) {

        // Si la canción existe, devuelve sus vecinos; de lo contrario, devuelve un mapa vacío.
        return adjList.getOrDefault(cancion, Collections.emptyMap());
    }
}
