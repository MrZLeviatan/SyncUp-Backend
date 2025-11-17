package co.edu.uniquindio.graph;

import co.edu.uniquindio.models.Cancion;
import co.edu.uniquindio.utils.collections.MiLinkedList;
import co.edu.uniquindio.utils.collections.MiMap;
import co.edu.uniquindio.utils.collections.MiSet;
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
    private final MiMap<Cancion, MiMap<Cancion, Double>> adjList = new MiMap<>();


    /**
     * Agrega una nueva canción (nodo) al grafo.
     * <p>Si la canción ya existe, no hace nada (debido a {@code putIfAbsent}).
     *
     * @param cancion La canción a agregar al grafo.
     */
    public void agregarCancion(Cancion cancion) {
        // Verifica si la canción ya está en el grafo
        if (adjList.get(cancion) == null) {
            // Si no existe, agrega la canción con una lista de adyacencia vacía usando MiMap
            adjList.put(cancion, new MiMap<>());
        }
    }


    /**
     * Elimina una canción (nodo) del grafo junto con todas sus conexiones (aristas).
     * <p>
     *
     * @param cancion La canción a eliminar del grafo. / The song to remove from the graph.
     */
    public void eliminarCancion(Cancion cancion) {
        // Verifica si la canción existe en el grafo
        MiMap<Cancion, Double> vecinos = adjList.get(cancion);
        if (vecinos == null) return; // Si no existe, no hace nada

        // Itera sobre todos los vecinos usando el iterator de MiMap
        for (MiMap.Par<Cancion, Double> par : vecinos) {
            Cancion vecino = par.key;
            // Remueve la canción de la lista de adyacencia del vecino
            MiMap<Cancion, Double> vecinosDelVecino = adjList.get(vecino);
            if (vecinosDelVecino != null) {
                vecinosDelVecino.remove(cancion);
            }
        }
        // Finalmente, elimina la canción del grafo
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
     * la suma de los pesos de las aristas (donde el peso es inversamente proporcional a la similitud).</p>
     *
     * @param origen La canción de inicio del camino.
     * @param destino La canción final del camino.
     * @return Una {@code MiLinkedList<Cancion>} que representa la secuencia del camino más corto, o una lista
     * parcial si el destino es inalcanzable desde el origen.
     */
    public MiLinkedList<Cancion> dijkstra(Cancion origen, Cancion destino) {

        // Mapa de distancias desde el origen a cada canción.
        // Usamos 'MiMap' para almacenar: Canción -> Distancia (costo).
        MiMap<Cancion, Double> dist = new MiMap<>();

        // Mapa de predecesores para reconstruir la ruta al final.
        // Usamos 'MiMap' para almacenar: Canción_Actual -> Canción_Anterior_en_ruta.
        MiMap<Cancion, Cancion> prev = new MiMap<>();

        // Inicializa todas las canciones del grafo con distancia máxima (simulando infinito).
        // 'adjList' es la lista de adyacencia del grafo (Cancion -> MiMap<Cancion, Double>).
        for (MiMap.Par<Cancion, MiMap<Cancion, Double>> par : adjList) {
            dist.put(par.key, Double.MAX_VALUE); // Asigna distancia infinita a todas las canciones iniciales.
        }

        // La distancia del origen a sí mismo es cero (punto de partida del algoritmo).
        dist.put(origen, 0.0);

        // Conjunto de canciones ya procesadas y con su distancia mínima finalizada.
        // Usamos 'MiSet' para una verificación rápida de visitados (aunque el add es O(n)).
        MiSet<Cancion> visitados = new MiSet<>();

        // Conjunto de canciones pendientes por procesar (similar a una cola de prioridad, pero implementada como Set/List).
        MiSet<Cancion> pendientes = new MiSet<>();
        pendientes.add(origen); // Agrega la canción de origen para comenzar el procesamiento.

        while (true) { // Bucle principal del algoritmo de Dijkstra.
            // Encontrar la canción con la menor distancia en el conjunto de pendientes.
            Cancion actual = null; // Variable para almacenar el nodo seleccionado en esta iteración.
            double minDist = Double.MAX_VALUE; // Variable para rastrear la menor distancia encontrada.

            // Simulación de extracción del mínimo: se recorre todo el conjunto pendiente.
            for (Cancion c : pendientes) { // Itera sobre las canciones pendientes.
                double d = dist.get(c); // Obtiene la distancia actual de la canción pendiente.
                if (d < minDist) { // Compara si la distancia es menor que la mínima actual.
                    minDist = d; // Actualiza la distancia mínima.
                    actual = c; // Asigna la canción como el nodo actual a procesar.
                }
            }

            if (actual == null) break; // Si no se encontró ningún nodo, significa que no quedan nodos por procesar (lista vacía).

            pendientes.remove(actual); // Elimina la canción seleccionada de los pendientes.
            visitados.add(actual); // Marca la canción como visitada/procesada.

            if (actual.equals(destino)) break; // Si el nodo actual es el destino, se encontró la ruta más corta y se termina.

            // Itera por todos los vecinos de la canción actual.
            // Se obtiene el mapa de adyacencia (vecinos y sus pesos) desde adjList.
            MiMap<Cancion, Double> vecinos = adjList.get(actual);
            if (vecinos == null) continue; // Si la canción no tiene vecinos, salta a la siguiente iteración del while.

            // Itera sobre los pares (Vecino -> Peso) del mapa de adyacencia.
            for (MiMap.Par<Cancion, Double> vecino : vecinos) {
                // Si el vecino ya está visitado, se ignora.
                if (visitados.contains(vecino.key)) continue;

                // Calcula la nueva distancia potencial: distancia del actual + peso de la arista.
                double nuevaDist = dist.get(actual) + vecino.value;

                // Comprueba si el vecino aún no tiene distancia (es null) o si la nueva ruta es mejor.
                if (dist.get(vecino.key) == null || nuevaDist < dist.get(vecino.key)) {
                    dist.put(vecino.key, nuevaDist); // Actualiza la distancia del vecino con la nueva distancia menor.
                    prev.put(vecino.key, actual); // Establece el nodo actual como el predecesor del vecino.
                    pendientes.add(vecino.key); // Añade el vecino a la lista de pendientes para su procesamiento (si no estaba).
                }
            }
        }

        // --------------------------------------------------------------------------------------
        // Reconstrucción de la Ruta
        // --------------------------------------------------------------------------------------

        // Reconstruir la ruta encontrada usando el mapa 'prev'.
        MiLinkedList<Cancion> ruta = new MiLinkedList<>(); // Lista temporal para construir la ruta al revés.
        Cancion paso = destino; // Inicia la reconstrucción desde el destino.

        // Itera hacia atrás usando el mapa de predecesores.
        while (paso != null) {
            ruta.add(paso); // Añade el nodo actual al final de la lista temporal.
            paso = prev.get(paso); // Obtiene el predecesor del nodo actual.
        }

        // Invertir la ruta para que vaya de origen a destino (actualmente está de destino a origen).
        MiLinkedList<Cancion> rutaFinal = new MiLinkedList<>(); // Lista final con la secuencia correcta.

        // Itera la lista temporal desde el final hasta el inicio.
        for (int i = ruta.size() - 1; i >= 0; i--) {
            rutaFinal.add(ruta.get(i)); // Añade los elementos en orden inverso para obtener el camino correcto.
        }

        return rutaFinal; // Retorna la lista con la ruta de menor costo.
    }


    /**
     * Devuelve el conjunto de todas las canciones (nodos) presentes en el grafo.
     *
     * <p>El método delega la obtención de las claves al método {@code keySet()} de la lista
     * de adyacencia interna del grafo.</p>
     *
     * @return Un {@link MiSet} de todas las {@code Cancion}es (nodos) del grafo.
     */
    public MiSet<Cancion> obtenerCanciones() {

        // Devuelve el conjunto de todas las llaves (canciones) en la lista de adyacencia.
        // Se asume que 'adjList' es un MiMap<Cancion, MiMap<Cancion, Double>> donde las claves son las canciones del grafo.
        return adjList.keySet(); // Llama al método keySet() del MiMap interno (adjList) y retorna el MiSet de canciones.
    }


    /**
     * Devuelve los vecinos directos de una canción dada y el peso de las aristas que los conectan.
     *
     * <p>Este método consulta la lista de adyacencia del grafo para encontrar las conexiones
     * de la canción especificada.</p>
     *
     * @param cancion La canción de la que se buscan los vecinos.
     * @return Un {@link MiMap} de vecinos (Canción) y sus pesos (Double); un mapa vacío si la canción no está en el grafo.
     */
    public MiMap<Cancion, Double> obtenerVecinos(Cancion cancion) {

        // Intenta obtener el mapa de adyacencia de la canción desde la lista de adyacencia (adjList).
        // Se asume que adjList es un MiMap<Cancion, MiMap<Cancion, Double>>.
        MiMap<Cancion, Double> vecinos = adjList.get(cancion);

        // Usa el operador ternario para verificar el resultado:
        // Si 'vecinos' no es null (la canción existe), retorna el mapa de vecinos.
        // Si 'vecinos' es null (la canción no existe o no tiene vecinos en la estructura principal), retorna un nuevo MiMap vacío.
        return (vecinos != null) ? vecinos : new MiMap<>();
    }

}
