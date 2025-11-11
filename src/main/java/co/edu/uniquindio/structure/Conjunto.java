package co.edu.uniquindio.structure;

import lombok.Getter;

/**
 * Implementación propia de un conjunto (Set) sin duplicados.
 * Clase genérica "Conjunto", que acepta cualquier tipo <T>.
 */
public class Conjunto<T> {

    /**
     * Nodo interno para la estructura enlazada.
     * Clase interna estática "Nodo" para representar cada elemento de la lista.
     */
    private static class Nodo<T> {

        // Valor almacenado en el nodo.
        T valor;

        // Referencia al siguiente nodo en la lista.
        Nodo<T> siguiente;

        // Constructor que recibe el valor inicial.
        Nodo(T valor) {

            // Asigna el valor recibido al atributo interno.
            this.valor = valor;

            // Inicializa la referencia "siguiente" como null.
            this.siguiente = null;
        }
    }

    // Nodo principal (inicio de la lista).
    private Nodo<T> cabeza;

    /**
     * -- GETTER --
     *  Retorna el número de elementos del conjunto.
     */ // Retorna el contador de tamaño.
    // Contador de elementos en el conjunto.
    @Getter
    private int tamanio;

    // Constructor sin parámetros.
    public Conjunto() {

        // Inicializa la cabeza como vacía.
        this.cabeza = null;

        // Inicializa el tamaño en 0.
        this.tamanio = 0;
    }

    /**
     * Agrega un elemento al conjunto si no existe ya.
     * Método público para insertar un nuevo elemento.
     */
    public void agregar(T valor) {

        // Si el valor es nulo, se sale sin hacer nada.
        if (valor == null) return;

        // Verifica si el elemento ya está en el conjunto.
        if (!contiene(valor)) {

            // Crea un nuevo nodo con el valor recibido.
            Nodo<T> nuevo = new Nodo<>(valor);

            // Apunta el nuevo nodo al nodo actual de la cabeza.
            nuevo.siguiente = cabeza;

            // Actualiza la cabeza para que apunte al nuevo nodo.
            cabeza = nuevo;

            // Incrementa el contador de tamaño.
            tamanio++;
        }
    }


    /**
     * Método público para eliminar un elemento.
     * Elimina un elemento del conjunto si existe.
     */
    public void eliminar(T valor) {

        // Si la lista está vacía, no hay nada que eliminar.
        if (cabeza == null) return;

        // Si el primer nodo contiene el valor a eliminar.
        if (cabeza.valor.equals(valor)) {

            // Desconecta el primer nodo y actualiza la cabeza.
            cabeza = cabeza.siguiente;

            // Reduce el tamaño del conjunto.
            tamanio--;

            // Finaliza la ejecución del método.
            return;
        }

        // Empieza desde la cabeza.
        Nodo<T> actual = cabeza;

        // Recorre la lista mientras no encuentre el valor.
        while (actual.siguiente != null && !actual.siguiente.valor.equals(valor)) {

            // Avanza al siguiente nodo.
            actual = actual.siguiente;
        }

        // Si encontró el nodo a eliminar.
        if (actual.siguiente != null) {

            // Salta el nodo objetivo, eliminándolo de la cadena.
            actual.siguiente = actual.siguiente.siguiente;

            // Disminuye el tamaño total.
            tamanio--;

        }
    }

    /**
     * Verifica si el conjunto contiene un valor.
     */
    public boolean contiene(T valor) {

        // Empieza desde la cabeza.
        Nodo<T> actual = cabeza;

        // Recorre todos los nodos hasta que no haya más
        while (actual != null) {

            // Si encuentra el valor buscado.
            if (actual.valor.equals(valor)) {

                // Retorna verdadero.
                return true;

            }

            // Avanza al siguiente nodo.
            actual = actual.siguiente;

        }

        // No se encontró el valor.
        return false;

    }

    /**
     * Devuelve todos los elementos como un arreglo.
     */
    public Object[] toArray() {

        // Crea un arreglo del tamaño del conjunto.
        Object[] arreglo = new Object[tamanio];


        // Empieza desde la cabeza.
        Nodo<T> actual = cabeza;

        // Índice para recorrer el arreglo.
        int i = 0;

        // Recorre todos los nodos.
        while (actual != null) {

            // Guarda cada valor en la posición correspondiente.
            arreglo[i++] = actual.valor;

            // Avanza al siguiente nodo.
            actual = actual.siguiente;

        }

        // Devuelve el arreglo final con todos los valores.
        return arreglo;

    }
}
