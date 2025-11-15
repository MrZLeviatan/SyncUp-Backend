package co.edu.uniquindio.graph;

import co.edu.uniquindio.models.Usuario;
import co.edu.uniquindio.utils.collections.MiLinkedList;
import co.edu.uniquindio.utils.collections.MiMap;
import co.edu.uniquindio.utils.collections.MiSet;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Representa un Grafo No Dirigido que modela las conexiones sociales (relaciones de amistad/seguimiento) entre {@link Usuario}s.
 *
 * <p>La estructura se basa en una lista de adyacencia implementada con {@code HashMap} y {@code HashSet}
 * para asegurar búsquedas eficientes (O(1)) y evitar la duplicación de conexiones.
 *
 * <p>Proporciona métodos esenciales para la gestión del grafo (agregar, conectar, desconectar)
 * y para la lógica de recomendación social (descubrimiento de "amigos de amigos" mediante BFS).
 *
 * @see Usuario
 */
@Component
public class GrafoSocial {


    /**
     * Estructura de Lista de Adyacencia del grafo.
     *
     * <p>Mapea cada {@link Usuario} (nodo) a un conjunto de {@link Usuario}s (vecinos) con los que está conectado.
     * <ul>
     * <li>Clave ({@code Usuario}): El nodo actual.</li>
     * <li>Valor ({@code Set<Usuario>}): Conjunto de vecinos directamente conectados.</li>
     * </ul>
     */
    private final MiMap<Usuario, MiSet<Usuario>> adyacencias = new MiMap<>();


    /**
     * Agrega un usuario (nodo) al grafo.
     *
     * <p>Si el usuario ya existe como clave en el mapa de adyacencias, la operación se ignora.
     * Si es un usuario nuevo, se le asocia un {@link MiSet} vacío que almacenará los {@link Usuario}s
     * que sigue, sirviendo como su lista de adyacencia.</p>
     *
     * @param usuario El usuario a agregar al grafo.
     */
    public void agregarUsuario(Usuario usuario) { // Define el método para agregar un usuario al grafo.

        // Comprobación de existencia: Si la clave 'usuario' ya existe en el mapa de adyacencias.
        if (adyacencias.get(usuario) != null) { // Usa el método get() de MiMap para verificar si el usuario ya está mapeado.
            return; // Si el usuario ya existe, termina el método sin hacer cambios.
        }

        // Si no existe, creamos su conjunto de adyacencias (MiSet propio).
        adyacencias.put(usuario, new MiSet<>()); // Usa el método put() de MiMap para añadir el usuario como clave, asociándolo a un nuevo MiSet vacío.
    }


    /**
     * Establece una conexión (arista) no dirigida entre dos usuarios.
     *
     * <p>La conexión se registra en ambas direcciones para mantener la propiedad de grafo no dirigido.
     * Si alguno de los usuarios no existe en el grafo, es agregado automáticamente.
     *
     * @param u1 Usuario origen de la conexión.
     * @param u2 Usuario destino de la conexión.
     */
    public void conectarUsuarios(Usuario u1, Usuario u2) {
        // Se aseguran de que ambos usuarios existan en el grafo.
        agregarUsuario(u1);
        agregarUsuario(u2);

        // Se agrega u2 a la lista de conexiones de u1.
        adyacencias.get(u1).add(u2);

        // Se agrega u1 a la lista de conexiones de u2 (porque el grafo NO es dirigido).
        adyacencias.get(u2).add(u1);
    }


    /**
     * Elimina la conexión (arista) entre dos usuarios.
     *
     * <p>Dado que se asume un grafo no dirigido (o de doble sentido en la adyacencia),
     * la operación de desconexión se realiza en ambas direcciones: eliminando a u2 de la lista
     * de adyacencia de u1, y eliminando a u1 de la lista de adyacencia de u2.</p>
     *
     * @param u1 Primer usuario (nodo).
     * @param u2 Segundo usuario (nodo).
     */
    public void desconectarUsuarios(Usuario u1, Usuario u2) { // Define el método para eliminar la arista entre dos usuarios.

        /* Buscamos el conjunto de vecinos para u1 */
        // Intenta obtener el MiSet de usuarios vecinos de u1 desde el mapa de adyacencias.
        MiSet<Usuario> vecinosU1 = adyacencias.get(u1);

        if (vecinosU1 != null) { // Verifica si u1 existe en el grafo (si su lista de vecinos no es null).
            // Llama al método remove() de MiSet para eliminar a u2 del conjunto de vecinos de u1.
            vecinosU1.remove(u2);
        }


        /* Buscamos el conjunto de vecinos para u2 */
        // Intenta obtener el MiSet de usuarios vecinos de u2 desde el mapa de adyacencias.
        MiSet<Usuario> vecinosU2 = adyacencias.get(u2);

        if (vecinosU2 != null) { // Verifica si u2 existe en el grafo.
            // Llama al método remove() de MiSet para eliminar a u1 del conjunto de vecinos de u2.
            vecinosU2.remove(u1);
        }
        // Dado que el grafo es no dirigido, con esto la desconexión queda completa.
    }


    /**
     * Obtiene los amigos de los amigos (distancia exactamente 2) usando el algoritmo BFS.
     *
     * <p>El método realiza un recorrido BFS limitado para encontrar todos los usuarios
     * que no son amigos directos del {@code origen} ni el {@code origen} mismo, pero que están
     * conectados a un amigo del {@code origen} (distancia = 2).</p>
     *
     * @param origen Usuario inicial (nodo de partida) del que se buscan sugerencias.
     * @return Una {@link MiLinkedList} con las sugerencias de amistad (usuarios a distancia 2).
     */
    public MiLinkedList<Usuario> obtenerAmigosDeAmigos(Usuario origen) { // Define el método que usa BFS para hallar amigos de amigos.

        // --- Verificar si el origen existe en el grafo --- //
        /* Si no existe en el MiMap, no hay conexiones */
        // Intenta obtener el MiSet de vecinos del usuario origen.
        MiSet<Usuario> vecinosOrigen = adyacencias.get(origen);
        if (vecinosOrigen == null) { // Si el usuario origen no está en el mapa de adyacencias.
            return new MiLinkedList<>(); // Retorna una lista vacía, ya que no tiene conexiones.
        }

        // --- Estructuras para BFS (Todas usan colecciones propias) --- //
        MiSet<Usuario> visitados = new MiSet<>();     // Conjunto para rastrear nodos ya visitados y evitar ciclos.
        MiLinkedList<Usuario> cola = new MiLinkedList<>();  // Cola FIFO para el proceso BFS.
        MiMap<Usuario, Integer> distancia = new MiMap<>();  // Mapa para almacenar la distancia (nivel) de cada nodo desde el origen.

        // --- Inicialización del BFS --- //
        cola.add(origen);            // Agrega el nodo de origen a la cola para empezar el recorrido.
        distancia.put(origen, 0);    // Asigna distancia 0 al nodo de origen.
        visitados.add(origen);       // Marca el nodo de origen como visitado.

        MiLinkedList<Usuario> sugerencias = new MiLinkedList<>(); // Lista donde se almacenarán los resultados (usuarios a distancia 2).

        // --- BFS --- //
        while (!cola.isEmpty()) { // Mientras la cola no esté vacía.

            Usuario actual = cola.removeFirst();      // Saca el primer nodo de la cola (operación O(1)).
            Integer nivelActual = distancia.get(actual); // Obtiene el nivel (distancia) del nodo que se acaba de sacar.

            if (nivelActual == null) continue; // Manejo de seguridad, aunque la inicialización garantiza que no será null.
            if (nivelActual >= 2) continue;           // Poda: No se necesita explorar nodos más allá del nivel 2.

            // Obtener vecinos del usuario actual
            MiSet<Usuario> vecinos = adyacencias.get(actual); // Obtiene el MiSet de vecinos del nodo actual.
            if (vecinos == null) continue;            // Si no tiene vecinos (aunque debería tener al menos si nivel > 0), continúa.

            // Itera sobre todos los vecinos del nodo actual (distancia nivelActual + 1).
            for (Usuario vecino : vecinos) {

                // Si el vecino aún no se ha visitado.
                if (!visitados.contains(vecino)) {

                    int nuevoNivel = nivelActual + 1; // Calcula la distancia del vecino (nivel + 1).
                    distancia.put(vecino, nuevoNivel); // Almacena la nueva distancia en el mapa.

                    cola.add(vecino);     // Encola el vecino.
                    visitados.add(vecino); // Marca el vecino como visitado.

                    // Condición de resultado: Si está exactamente a distancia 2.
                    if (nuevoNivel == 2) {
                        sugerencias.add(vecino); // Agrega el vecino a la lista de sugerencias.
                    }
                }
            }
        }

        return sugerencias; // Retorna la lista de usuarios encontrados a distancia 2.
    }



    /**
     * Devuelve la estructura interna de adyacencias del grafo.
     *
     * @return Un {@code Map} que representa el grafo completo.
     */
    public MiMap<Usuario, MiSet<Usuario>> obtenerEstructura() {
        // Retorna el mapa completo con todos los usuarios y sus conexiones.
        return adyacencias;
    }
}
