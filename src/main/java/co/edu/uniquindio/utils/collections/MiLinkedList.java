package co.edu.uniquindio.utils.collections;

import java.util.Iterator;

/**
 * Implementación propia de una estructura de datos tipo Lista Enlazada (LinkedList) genérica.
 *
 * <p>Esta lista permite almacenar elementos de cualquier tipo y es iterable,
 * implementando la interfaz {@code Iterable<T>}.</p>
 *
 * @param <T> El tipo de elementos que contendrá la lista.
 */
public class MiLinkedList<T> implements Iterable<T> {

    /**
     * Referencia al primer nodo de la lista (cabeza).
     * Si es {@code null}, la lista está vacía.
     */
    private Nodo<T> head; // Almacena el nodo inicial o "cabeza" de la lista.

    /**
     * Contador que lleva el registro del número actual de elementos en la lista.
     */
    private int size = 0; // Almacena el número de elementos en la lista.


    /**
     * Clase anidada estática que representa un nodo individual en la lista enlazada.
     *
     * @param <T> El tipo del valor almacenado en el nodo.
     */
    private static class Nodo<T> { // Clase interna estática que representa un nodo de la lista.
        T valor;// El valor de dato almacenado en este nodo.
        Nodo<T> siguiente; // Referencia al siguiente nodo en la secuencia (enlace).

        /**
         * Constructor que inicializa el nodo con un valor.
         *
         * @param valor El valor a almacenar.
         */
        Nodo(T valor) { // Constructor del nodo.
            this.valor = valor; // Inicializa el valor del nodo.
        }
    }


    /**
     * Añade un nuevo valor al final de la lista.
     *
     * <p>La complejidad temporal es O(n) en el peor caso, ya que debe recorrer la lista
     * hasta encontrar el último nodo. La lógica de recorrido se delega al método
     * privado {@code addRecursivo}.</p>
     *
     * @param valor El valor a añadir.
     */
    public void add(T valor) { // Método público para añadir un elemento a la lista.
        if (head == null) { // Verifica si la lista está vacía (la cabeza es null).
            head = new Nodo<>(valor); // Si está vacía, crea un nuevo nodo y lo asigna como cabeza.
        } else { // Si la lista no está vacía:
            addRecursivo(head, valor); // Llama al método auxiliar recursivo, iniciando desde la cabeza.
        }
        size++; // Incrementa el contador de tamaño de la lista, ya que se añadió un elemento.
    }


    /**
     * Método auxiliar privado que recorre la lista de forma recursiva para añadir un nodo al final.
     *
     * <p>La recursión finaliza cuando se encuentra el último nodo (aquel cuyo puntero {@code siguiente} es null).</p>
     *
     * @param actual El nodo desde donde se inicia la verificación en la llamada actual.
     * @param valor El valor a añadir al final de la lista.
     */
    private void addRecursivo(Nodo<T> actual, T valor) { // Método auxiliar recursivo para la inserción.
        if (actual.siguiente == null) { // Caso base de la recursión: si el nodo actual es el último (su siguiente es null).
            actual.siguiente = new Nodo<>(valor); // Crea el nuevo nodo con el valor y lo enlaza al final.
        } else { // Paso recursivo: si el nodo actual no es el último.
            addRecursivo(actual.siguiente, valor); // Llama recursivamente con el siguiente nodo para continuar el recorrido.
        }
    }


    /**
     * Elimina la primera aparición del valor especificado de esta lista, si está presente.
     *
     * <p>La complejidad temporal es O(n) en el peor caso, ya que puede requerir recorrer toda la lista.</p>
     *
     * @param valor El valor a eliminar de la lista.
     */
    public void remove(T valor) { // Método público para eliminar un elemento por valor.
        if (head == null) // Verifica si la lista está vacía.
            return; // Si está vacía, no hay nada que eliminar, termina el método.

        // Caso especial: eliminar cabeza
        if (head.valor.equals(valor)) { // Comprueba si el valor a eliminar está en la cabeza.
            head = head.siguiente; // Si está, la cabeza pasa a ser el nodo siguiente.
            size--; // Decrementa el contador de tamaño.
            return; // Termina la eliminación.
        }

        // Llamada recursiva para el resto
        removeRecursivo(head, valor); // Llama al método auxiliar recursivo, comenzando la búsqueda desde la cabeza.
    }


    /**
     * Método auxiliar privado que recorre la lista de forma recursiva para eliminar un nodo.
     *
     * <p>El método busca el valor en el nodo *siguiente* al actual para poder re-enlazar correctamente.</p>
     *
     * @param actual El nodo desde el cual se evalúa el nodo siguiente.
     * @param valor El valor a buscar y eliminar.
     */
    private void removeRecursivo(Nodo<T> actual, T valor) {

        // Caso base: siguiente es null -> terminó
        if (actual.siguiente == null) // Verifica si el siguiente nodo es null (fin de la lista).
            return; // Si es el final, el elemento no está presente, termina la recursión.

        // Si el siguiente nodo tiene el valor, eliminarlo
        if (actual.siguiente.valor.equals(valor)) { // Comprueba si el valor a eliminar está en el nodo *siguiente* al actual.
            // Re-enlaza: el siguiente del nodo actual pasa a ser el siguiente del nodo que se va a eliminar.
            actual.siguiente = actual.siguiente.siguiente;
            size--; // Decrementa el contador de tamaño.
            return; // Termina la recursión después de la eliminación.
        }

        // Seguir buscando recursivamente
        removeRecursivo(actual.siguiente, valor); // Llama recursivamente con el siguiente nodo para continuar la búsqueda.
    }



    /**
     * Retorna el número de elementos en esta lista.
     *
     * <p>La complejidad temporal es O(1).</p>
     *
     * @return El tamaño actual de la lista.
     */
    public int size() { // Método para obtener el tamaño de la lista.
        return size; // Retorna el contador de tamaño.
    }


    /**
     * Retorna el elemento en la posición especificada por el índice.
     *
     * <p>La complejidad temporal es O(n) en el peor caso, ya que requiere recorrer
     * la lista hasta la posición del índice.</p>
     *
     * @param index El índice del elemento a retornar (basado en cero).
     * @return El valor almacenado en esa posición.
     * @throws IndexOutOfBoundsException Si el índice está fuera del rango [0, size-1].
     */
    public T get(int index) { // Método para obtener un elemento por índice.
        if (index < 0 || index >= size) // Verifica si el índice está fuera de los límites.
            throw new IndexOutOfBoundsException(); // Lanza excepción si el índice es inválido.

        Nodo<T> actual = head; // Inicia el recorrido desde la cabeza.

        for (int i = 0; i < index; i++) // Itera hasta alcanzar el índice deseado.
            actual = actual.siguiente; // Mueve el puntero al siguiente nodo.

        return actual.valor; // Retorna el valor del nodo encontrado en el índice.
    }


    /**
     * Retorna un iterator sobre los elementos de esta lista en la secuencia correcta.
     *
     * @return Un objeto {@link Iterator} para recorrer la lista.
     */
    @Override // Sobrescribe el método de la interfaz Iterable<T>.
    public Iterator<T> iterator() { // Implementa el método que permite iterar.
        return new Iterator<>() { // Retorna una instancia de una clase anónima que implementa Iterator.
            Nodo<T> actual = head; // El nodo actual comienza en la cabeza.

            /**
             * Verifica si hay más elementos para iterar.
             */
            public boolean hasNext() { // Verifica si hay un siguiente elemento.
                return actual != null; // Hay siguiente si el nodo actual no es null.
            }

            /**
             * Retorna el siguiente elemento en la iteración.
             */
            public T next() { // Retorna el valor del nodo actual y avanza.
                T val = actual.valor; // Almacena el valor del nodo actual.
                actual = actual.siguiente; // Mueve el puntero al siguiente nodo.
                return val; // Retorna el valor que fue almacenado.
            }
        };
    }
}
