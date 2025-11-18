package co.edu.uniquindio.utils.listasPropias;

import java.util.Iterator;

/**
 * Implementación propia de una estructura de datos tipo Conjunto (Set) genérica.
 *
 * <p>Esta clase asegura que solo se almacenen elementos únicos, delegando el almacenamiento
 * y la iteración a la estructura {@link MiLinkedList}.</p>
 *
 * <p>Debido a que utiliza una lista enlazada para la verificación de unicidad,
 * las operaciones {@code add} y {@code contains} tienen una complejidad temporal de O(n).</p>
 *
 * @param <T> El tipo de elementos que contendrá el conjunto.
 */
public class MiSet<T> implements Iterable<T> {

    /**
     * Instancia de {@link MiLinkedList} utilizada internamente para el almacenamiento real de los elementos.
     */
    private MiLinkedList<T> lista = new MiLinkedList<>(); // Delegación del almacenamiento a la lista enlazada propia.

    /**
     * Añade el valor especificado al conjunto si aún no está presente.
     *
     * <p>Si el valor ya existe (verificado mediante {@code equals}), la operación es ignorada.</p>
     *
     * @param valor El valor a añadir.
     */
    public void add(T valor) {
        // Itera sobre todos los elementos de la lista.
        for (T v : lista)
            // Si el valor ya existe en la lista:
            if (v.equals(valor)) return; // No se añade y se sale del método (mantiene la unicidad del Set).
        // Si el valor no fue encontrado, se añade a la lista.
        lista.add(valor);
    }

    /**
     * Retorna {@code true} si este conjunto contiene el valor especificado.
     *
     * @param valor El valor cuya presencia se va a verificar.
     * @return {@code true} si el valor está presente, {@code false} en caso contrario.
     */
    public boolean contains(T valor) {
        // Itera sobre todos los elementos para buscar el valor.
        for (T v : lista)
            // Si el valor es encontrado:
            if (v.equals(valor))
                return true; // Retorna true inmediatamente.

        // Si el bucle termina sin encontrar el valor:
        return false; // Retorna false.
    }

    /**
     * Elimina un valor del conjunto si existe.
     *
     * @param valor Elemento a eliminar
     */
    public void remove(T valor) {
        // Delegamos a la lista interna, que ya tiene remove()
        lista.remove(valor);
    }


    public boolean isEmpty() {
        return !lista.iterator().hasNext();
    }


    /**
     * Retorna un iterator sobre los elementos del conjunto en la secuencia de inserción.**
     *
     * <p>Delega la implementación del iterator a la lista interna.</p>
     *
     * @return Un objeto {@link Iterator} para recorrer el conjunto.
     */
    @Override
    public Iterator<T> iterator() {
        // Delega la responsabilidad de la iteración a la MiLinkedList interna.
        return lista.iterator();
    }
}
