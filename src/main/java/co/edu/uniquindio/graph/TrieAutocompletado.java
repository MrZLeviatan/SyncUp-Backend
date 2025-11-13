package co.edu.uniquindio.graph;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Clase que implementa un Árbol de Prefijos (Trie) para la función de autocompletado de títulos de canciones.
 *
 * <p>El Trie es como un *"diccionario en forma de árbol"* donde cada nodo representa una letra.
 * Las palabras se construyen recorriendo las ramas del árbol, letra por letra, desde la raíz.</p>
 *
 * <p>Esta estructura es extremadamente eficiente para buscar todas las palabras (títulos)
 * que comienzan con una secuencia de letras (un prefijo) dada.</p>
 */
@Component
public class TrieAutocompletado {

    // Nodo raíz del Trie. Es el punto de partida de toda búsqueda o inserción.
    // Piensa en él como la primera página vacía de un índice.
    private final NodoTrie raiz;


    /**
     * Constructor del Trie.
     * <p> Inicializa la raíz como un nodo vacío, ya que la raíz no representa ninguna letra real,
     * solo es el ancla del árbol.
     */
    public TrieAutocompletado() {
        raiz = new NodoTrie();
    }


    /**
     * Inserta una palabra (título de canción) dentro del Trie.
     *
     * <p>El proceso traza el camino de la palabra en el árbol, creando nodos para las letras
     * que aún no existen en la secuencia.</p>
     *
     * @param palabra La palabra que se desea almacenar en el Trie (ej. "Amapola").
     */
    public void insertar(String palabra) {
        // Empezamos siempre desde la raíz, el punto de inicio.
        NodoTrie actual = raiz;

        // Recorremos la palabra letra por letra. Convertimos a minúsculas para que la búsqueda sea insensible a mayúsculas/minúsculas.
        for (char c : palabra.toLowerCase().toCharArray()) {

            // Si la letra actual ('c') no tiene un nodo hijo asociado al nodo actual, ¡lo creamos!
            // Esto asegura que cada letra de la palabra tenga su propio lugar en el árbol.
            actual.hijos.putIfAbsent(c, new NodoTrie());

            // Una vez que sabemos que el nodo existe (ya sea nuevo o preexistente), avanzamos a él.
            actual = actual.hijos.get(c);
        }

        // Al finalizar el bucle, hemos llegado al final de la palabra.
        // Marcamos este nodo final para indicar que la secuencia de letras hasta aquí forma una palabra completa.
        actual.esFinDePalabra = true;
    }


    /**
     * Busca todas las palabras almacenadas que coinciden con un prefijo específico (Función de Autocompletado).
     *
     * <p>Primero recorre el árbol hasta el último carácter del prefijo. Luego, explora
     * todas las ramas que se extienden desde ese punto.</p>
     *
     * @param prefijo Prefijo que se desea autocompletar (ej. "ama").
     * @return Lista de palabras completas que comienzan con el prefijo dado (ej. ["amapola", "amargo"]).
     */
    public List<String> autocompletar(String prefijo) {
        // Empezamos desde la raíz para buscar el nodo donde termina el prefijo.
        NodoTrie nodo = raiz;

        // 1. Recorrer el Trie siguiendo las letras del prefijo.
        for (char c : prefijo.toLowerCase().toCharArray()) {

            // Si el nodo actual no tiene un hijo con la letra 'c', la búsqueda falla.
            // Esto significa que no hay títulos que comiencen con este prefijo.
            if (!nodo.hijos.containsKey(c)) {
                return Collections.emptyList(); // Devolvemos una lista vacía.
            }
            // Si la letra existe, avanzamos a ese nodo hijo para buscar la siguiente letra.
            nodo = nodo.hijos.get(c);
        }

        // 2. Si llegamos hasta aquí, el prefijo completo existe. ¡Es hora de explorar las coincidencias!
        List<String> resultados = new ArrayList<>();

        // Llamamos a la función auxiliar 'dfs' (Búsqueda en Profundidad) para encontrar todas las palabras que se
        // ramifican desde el nodo donde terminó el prefijo.
        dfs(nodo, prefijo.toLowerCase(), resultados);

        // Devolvemos la lista final de sugerencias.
        return resultados;
    }


    /**
     * Función auxiliar recursiva para explorar todas las ramas que descienden de un nodo (Búsqueda en Profundidad - DFS).
     *
     * <p>Esta función se encarga de construir las palabras completas que se pueden formar
     * a partir del nodo donde finalizó el prefijo.</p>
     *
     * @param nodo Nodo actual desde el cual se sigue la exploración.
     * @param palabra Prefijo acumulado hasta el momento. En cada llamada, se añade una letra.
     * @param resultados Lista donde se guardan las palabras completas que se encuentran.
     */
    private void dfs(NodoTrie nodo, String palabra, List<String> resultados) {

        // Si el nodo actual marca el final de una palabra completa, la agregamos a los resultados.
        if (nodo.esFinDePalabra) {
            resultados.add(palabra);
        }

        // Recorremos cada letra posible que puede continuar la palabra desde este nodo.
        for (Map.Entry<Character, NodoTrie> entry : nodo.hijos.entrySet()) {

            // Llamamos a la función recursivamente:
            // 1. Pasamos el nodo hijo (entry.getValue()) como nuevo punto de partida.
            // 2. Concatenamos la letra actual (entry.getKey()) a la palabra que estamos construyendo.
            dfs(entry.getValue(), palabra + entry.getKey(), resultados);
        }
    }

    /**
     * Clase interna que representa la unidad básica (cada letra o paso) del Árbol de Prefijos.
     *
     * <p>Cada {@code NodoTrie} actúa como un punto de bifurcación en el árbol.</p>
     */
    private static class NodoTrie {

        // Mapa que almacena los nodos "hijos". La clave es el carácter (letra) y el valor es el siguiente nodo.
        // Piensa en esto como un índice para saber qué camino tomar para cada letra siguiente.
        Map<Character, NodoTrie> hijos = new HashMap<>();

        // Indica si la secuencia de letras desde la raíz hasta este nodo forma una palabra completa y válida.
        boolean esFinDePalabra = false;
    }
}



