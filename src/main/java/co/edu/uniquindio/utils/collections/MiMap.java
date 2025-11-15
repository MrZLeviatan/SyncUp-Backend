package co.edu.uniquindio.utils.collections;
import java.util.Iterator;

/**
 * Implementación propia de una estructura de datos tipo Mapa (Map) simple utilizando una Lista Enlazada.
 *
 * <p>Almacena par clave-valor (Key-Value) y soporta la iteración sobre dichos pares.
 * En esta implementación, la lista no está optimizada para la búsqueda (es O(n)).</p>
 *
 * @param <K> El tipo de las claves (Keys).
 * @param <V> El tipo de los valores (Values).
 */
public class MiMap<K,V> implements Iterable<MiMap.Par<K,V>> { // Clase genérica que implementa Iterable para poder usar for-each.

    /**
     * Clase anidada estática que representa un nodo individual en la lista enlazada del mapa.
     *
     * @param <K> El tipo de la clave.
     * @param <V> El tipo del valor.
     */
    private static class Nodo<K,V> { // Clase interna estática que representa un nodo (elemento) del Map.
        K key; // La clave (Key) almacenada en este nodo.
        V value; // El valor (Value) asociado a la clave.
        Nodo<K,V> siguiente; // Referencia al siguiente nodo en la lista.

        /**
         * Constructor que inicializa el nodo con clave y valor.
         */
        Nodo(K k, V v){ // Constructor del nodo.
            key=k; // Inicializa la clave.
            value=v; // Inicializa el valor.
        }
    }

    /**
     * Clase pública anidada que representa un Par (Key-Value) inmutable, usada para la iteración.
     *
     * @param <K> El tipo de la clave.
     * @param <V> El tipo del valor.
     */
    public static class Par<K,V> { // Clase estática pública para representar el par de datos.
        public K key; // Clave del par.
        public V value; // Valor del par.

        /**
         * Constructor que inicializa el par.
         */
        public Par(K k, V v){ // Constructor del par.
            this.key=k; // Inicializa la clave.
            this.value=v; // Inicializa el valor.
        }
    }

    /**
     * Referencia al primer nodo de la lista (cabeza), que representa el inicio del mapa.
     */
    private Nodo<K,V> head; // Almacena el nodo inicial o "cabeza" de la lista enlazada.

    /**
     * Asocia el valor especificado con la clave especificada en este mapa.
     *
     * <p>Si la clave ya existe, el valor anterior se reemplaza (put de actualización).
     * Si la clave es nueva, se añade al inicio de la lista (put de inserción, O(1) si no existe).</p>
     *
     * @param key La clave con la que se asociará el valor.
     * @param value El valor a ser asociado.
     */
    public void put(K key, V value) { // Método para insertar o actualizar un par clave-valor.
        Nodo<K,V> actual = head; // Inicia la búsqueda desde la cabeza.

        while (actual != null) { // Recorre la lista para buscar una clave existente.
            if (actual.key.equals(key)) { // Si la clave ya existe:
                actual.value = value; // Actualiza el valor.
                return; // Termina el método.
            }
            actual = actual.siguiente; // Avanza al siguiente nodo.
        }

        // Si la clave no fue encontrada (bucle terminado):
        Nodo<K,V> nodo = new Nodo<>(key,value); // Crea un nuevo nodo.
        nodo.siguiente = head; // Establece el siguiente del nuevo nodo como la cabeza actual (inserción al inicio).
        head = nodo; // El nuevo nodo se convierte en la nueva cabeza.
    }

    /**
     * Retorna el valor al que está asociada la clave especificada.
     *
     * <p>La complejidad es O(n) en el peor caso, ya que requiere recorrer la lista.</p>
     *
     * @param key La clave cuyo valor asociado se va a retornar.
     * @return El valor asociado a la clave, o {@code null} si la clave no se encuentra.
     */
    public V get(K key) { // Método para obtener el valor asociado a una clave.
        Nodo<K,V> actual = head; // Inicia la búsqueda.

        while (actual != null) { // Recorre la lista.
            if (actual.key.equals(key)) // Si encuentra la clave:
                return actual.value; // Retorna el valor asociado.
            actual = actual.siguiente; // Avanza al siguiente nodo.
        }
        return null; // Retorna null si la clave no fue encontrada después de recorrer toda la lista.
    }

    /**
     * Elimina la asociación para una clave de este mapa, si está presente.
     *
     * <p>La complejidad es O(n) en el peor caso.</p>
     *
     * @param key La clave de la asociación a eliminar.
     */
    public void remove(K key) { // Método para eliminar un par clave-valor por clave.
        Nodo<K,V> actual = head; // Puntero al nodo actual.
        Nodo<K,V> prev = null; // Puntero al nodo anterior, inicializado en null.

        while (actual != null) { // Recorre la lista.
            if (actual.key.equals(key)) { // Si encuentra la clave a eliminar:
                if (prev == null) // Caso 1: Si el nodo a eliminar es la cabeza (prev es null).
                    head = actual.siguiente; // La nueva cabeza es el siguiente nodo.
                else // Caso 2: El nodo a eliminar está en medio o al final.
                    prev.siguiente = actual.siguiente; // El nodo anterior se enlaza con el siguiente del nodo actual.
                return; // Termina la eliminación (solo hay una clave igual).
            }
            prev = actual; // Actualiza el nodo anterior al nodo actual.
            actual = actual.siguiente; // Avanza al siguiente nodo.
        }
    }


    /**
     * **Retorna un conjunto que contiene todas las claves (Keys) de este mapa.**
     *
     * <p>La complejidad temporal es O(n) debido al recorrido de la lista enlazada, donde n es el
     * número de elementos en el mapa.</p>
     *
     * @return Un {@link MiSet} con todas las claves del mapa.
     */
    public MiSet<K> keySet() {

        MiSet<K> conjunto = new MiSet<>(); // 1. Inicializa un nuevo conjunto vacío (MiSet) para almacenar las claves.

        Nodo<K,V> actual = head; // 2. Comienza el recorrido desde el nodo cabeza (head) del mapa.

        while (actual != null) { // 3. Itera mientras el nodo actual no sea nulo (recorre toda la lista).
            conjunto.add(actual.key); // 4. Añade la clave (key) del nodo actual al conjunto de claves.
            actual = actual.siguiente; // 5. Avanza al siguiente nodo en la lista.
        }

        return conjunto; // 6. Retorna el conjunto con todas las claves recopiladas.
    }


    /**
     * Retorna el valor asociado a la clave dada, o un valor por defecto si la clave no existe.
     *
     * <p>Es una alternativa segura al método {@code get} que evita retornar {@code null}
     * cuando la clave no está presente en el mapa.</p>
     *
     * @param key La clave cuyo valor asociado se va a retornar.
     * @param valorPorDefecto El valor a retornar si la clave no se encuentra.
     * @return El valor asociado a la clave, o {@code valorPorDefecto} si la clave no existe.
     */
    public V getOrDefault(K key, V valorPorDefecto) {
        V valor = get(key); // Intenta obtener el valor asociado a la clave llamando al método get().

        // Usa el operador ternario para verificar el resultado de la obtención:
        // Si 'valor' no es null (clave encontrada), retorna 'valor'.
        // Si 'valor' es null (clave no encontrada), retorna 'valorPorDefecto'.
        return (valor != null) ? valor : valorPorDefecto;
    }



    /**
     * Retorna un iterator sobre los pares clave-valor de este mapa.
     *
     * @return Un objeto {@link Iterator} para recorrer los pares (Key, Value).
     */
    @Override // Sobreescribe el método de la interfaz Iterable.
    public Iterator<Par<K,V>> iterator() { // Implementa el método que permite iterar.
        return new Iterator<>() { // Retorna una instancia de una clase anónima que implementa Iterator.
            Nodo<K,V> actual = head; // El nodo actual comienza en la cabeza.

            /**
             * Verifica si hay más elementos para iterar.
             */
            public boolean hasNext() { // Verifica si hay un siguiente elemento.
                return actual!=null; // Hay siguiente si el nodo actual no es null.
            }

            /**
             * Retorna el siguiente par (clave, valor) en la iteración.
             */
            public Par<K,V> next() { // Retorna el valor del nodo actual y avanza.
                // Crea un nuevo Par inmutable con los datos del nodo actual.
                Par<K,V> p = new Par<>(actual.key, actual.value);
                actual = actual.siguiente; // Mueve el puntero al siguiente nodo.
                return p; // Retorna el par (Key, Value).
            }
        };
    }
}