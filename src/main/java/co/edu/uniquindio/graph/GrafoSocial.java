package co.edu.uniquindio.graph;

import co.edu.uniquindio.models.Usuario;

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
    private final Map<Usuario, Set<Usuario>> adyacencias = new HashMap<>();


    /**
     * Agrega un {@link Usuario} (nodo) al grafo.
     *
     * <p>Si el usuario ya existe, el método no realiza ninguna acción. Si es nuevo,
     * lo inicializa con un conjunto vacío de adyacencias.
     *
     * @param usuario El usuario a agregar.
     */
    public void agregarUsuario(Usuario usuario) {
        // putIfAbsent agrega al mapa solo si no existe una entrada con esa clave (usuario).
        adyacencias.putIfAbsent(usuario, new HashSet<>());
        // Si ya existe, no hace nada; si no, crea un nuevo conjunto vacío de conexiones.
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
     * <p>Elimina la relación en ambas direcciones (u1 a u2 y u2 a u1).
     *
     * @param u1 Primer usuario involucrado.
     * @param u2 Segundo usuario involucrado.
     */
    public void desconectarUsuarios(Usuario u1, Usuario u2) {
        // Verifica si u1 está presente en el grafo y elimina la conexión hacia u2.
        if (adyacencias.containsKey(u1)) adyacencias.get(u1).remove(u2);
        // Verifica si u2 está presente y elimina la conexión hacia u1.
        if (adyacencias.containsKey(u2)) adyacencias.get(u2).remove(u1);
    }


    /**
     * Realiza un recorrido de Búsqueda en Amplitud (Breadth-First Search, BFS) para encontrar sugerencias de amistad.
     *
     * <p>Identifica a los usuarios que están exactamente a una distancia de *dos aristas* del usuario origen
     * (los "amigos de amigos"). El algoritmo asegura que los usuarios directamente conectados (distancia 1)
     * y el propio origen no sean incluidos en la lista de sugerencias.
     *
     * @param origen Él {@link Usuario} desde el cual se inicia la búsqueda.
     * @return Una {@code List<Usuario>} que contiene los usuarios sugeridos (distancia 2).
     */
    public List<Usuario> obtenerAmigosDeAmigos(Usuario origen) {
        // Si el usuario no está en el grafo, devuelve una lista vacía (no hay conexiones).
        if (!adyacencias.containsKey(origen)) return Collections.emptyList();

        // Conjunto que almacenará los usuarios ya visitados (para evitar ciclos).
        Set<Usuario> visitados = new HashSet<>();

        // Cola utilizada para recorrer el grafo en anchura (BFS).
        Queue<Usuario> cola = new LinkedList<>();

        // Mapa que guarda la distancia (nivel) de cada usuario respecto al origen.
        Map<Usuario, Integer> distancia = new HashMap<>();

        // Se inicializa el recorrido agregando el usuario origen en la cola.
        cola.add(origen);

        // La distancia del usuario origen a sí mismo es 0.
        distancia.put(origen, 0);

        // Marca el usuario origen como visitado.
        visitados.add(origen);

        // Lista donde se almacenarán los usuarios sugeridos (nivel 2 del BFS).
        List<Usuario> sugerencias = new ArrayList<>();

        // Mientras haya elementos en la cola (usuarios por explorar)...
        while (!cola.isEmpty()) {

            // Extrae el siguiente usuario de la cola (FIFO).
            Usuario actual = cola.poll();

            // Obtiene la distancia actual desde el origen hasta este usuario.
            int nivel = distancia.get(actual);

            if (nivel >= 2) continue; // Solo buscamos hasta distancia 2

            // Recorre todos los vecinos (usuarios conectados) del usuario actual.
            for (Usuario vecino : adyacencias.getOrDefault(actual, Set.of())) {

                // Si el vecino aún no ha sido visitado...
                if (!visitados.contains(vecino)) {

                    // Registra su distancia (nivel actual + 1).
                    distancia.put(vecino, nivel + 1);

                    // Encola al vecino para continuar el recorrido BFS.
                    cola.add(vecino);

                    // Marca al vecino como visitado.
                    visitados.add(vecino);

                    // Si el vecino está exactamente a distancia 2, lo consideramos “amigo de amigo”.
                    if (distancia.get(vecino) == 2) {
                        sugerencias.add(vecino);
                    }
                }
            }
        }
        // Devuelve la lista final de usuarios sugeridos (nivel 2 del recorrido BFS).
        return sugerencias;
    }


    /**
     * Devuelve la estructura interna de adyacencias del grafo.
     *
     * @return Un {@code Map} que representa el grafo completo.
     */
    public Map<Usuario, Set<Usuario>> obtenerEstructura() {
        // Retorna el mapa completo con todos los usuarios y sus conexiones.
        return adyacencias;
    }
}
