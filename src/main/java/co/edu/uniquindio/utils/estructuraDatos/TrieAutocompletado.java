package co.edu.uniquindio.utils.estructuraDatos;

import co.edu.uniquindio.utils.listasPropias.MiLinkedList;
import co.edu.uniquindio.utils.listasPropias.MiMap;
import org.springframework.stereotype.Component;

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

            // Intentar obtener el hijo asociado a este carácter usando MiMap.
            NodoTrie hijo = actual.hijos.get(c);

            if (hijo == null) { // Si el nodo hijo para este carácter no existe:
                // Si no existe, crearlo.
                hijo = new NodoTrie(); // Crea un nuevo nodo.
                actual.hijos.put(c, hijo); // Asocia el carácter 'c' con el nuevo nodo hijo en el MiMap.
            }

            actual = hijo; // Se mueve al nodo hijo para la siguiente iteración.
        }
        // Cuando termina la palabra, marca el último nodo como fin de una palabra válida.
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
    public MiLinkedList<String> autocompletar(String prefijo) {
        // Empezamos desde la raíz para buscar el nodo donde termina el prefijo.
        NodoTrie nodo = raiz;

        // 1. Recorrer el Trie siguiendo las letras del prefijo.
        for (char c : prefijo.toLowerCase().toCharArray()) {
            // Intenta obtener el siguiente nodo para el carácter actual 'c'.
            NodoTrie siguiente = nodo.hijos.get(c);

            if (siguiente == null) { // Si el siguiente nodo es nulo:
                return new MiLinkedList<>(); // El prefijo no existe en el Trie, retorna una lista vacía.
            }
            nodo = siguiente; // Avanza al siguiente nodo del prefijo.
        }

        // Si el prefijo existe, explorar resultados
        MiLinkedList<String> resultados = new MiLinkedList<>(); // Lista propia para almacenar las palabras encontradas.
        // Inicia el DFS desde el último nodo alcanzado (final del prefijo).
        dfs(nodo, prefijo.toLowerCase(), resultados);

        return resultados; // Retorna la lista de palabras autocompletadas.
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
    private void dfs(NodoTrie nodo, String palabra, MiLinkedList<String> resultados) {

        // Si el nodo actual marca el final de una palabra completa, la agregamos a los resultados.
        if (nodo.esFinDePalabra) {
            // Añade la palabra construida hasta ahora a la lista de resultados.
            resultados.add(palabra);
        }

        // Recorrer todos los hijos usando el iterador del MiMap (MiMap.Par).
        for (MiMap.Par<Character, NodoTrie> par : nodo.hijos) {
            // Llamada recursiva:
            // Continúa el DFS con el nodo hijo (par.value), añadiendo el carácter del hijo (par.key) a la palabra actual.
            dfs(par.value, palabra + par.key, resultados);
        }
    }


    /**
     * Clase interna que representa la unidad básica (cada letra o paso) del Árbol de Prefijos.
     *
     * <p>Cada {@code NodoTrie} actúa como un punto de bifurcación en el árbol.</p>
     */
    private static class NodoTrie {

        // Mapa propio <Character, NodoTrie> para las transiciones.
        MiMap<Character, NodoTrie> hijos = new MiMap<>();

        // Marca el final de una palabra válida (un nodo puede ser fin de palabra y tener hijos).
        boolean esFinDePalabra = false;
    }
}



