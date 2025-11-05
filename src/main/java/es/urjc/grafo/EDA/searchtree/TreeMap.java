package es.urjc.grafo.EDA.searchtree;

import es.urjc.grafo.EDA.map.AbstractSortedMap;
import es.urjc.grafo.EDA.tree.LinkedBinaryTree;
import es.urjc.grafo.EDA.util.Entry;
import es.urjc.grafo.EDA.util.Position;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of a sorted map using a binary search tree.
 *
 * @param <K> The key type (keys must be unique and comparable)
 * @param <V> The value type
 * <p>
 * Internamente, el TreeMap utiliza un LinkedBinaryTree para almacenar las entradas.
 * El único detalle a tener en cuenta es la propiedad de relación: cada entrada se almacena en un nodo interno,
 * y la clave de cada nodo es mayor que la de su hijo izquierdo y menos que la de su hijo derecho.
 * Además, los nodos hoja (externos) no almacenan entradas y actúan como nodos centinela para facilitar las operaciones de inserción y eliminación.
 * Es decir, los nodos hoja son nodos nulos que indican el final de una rama del árbol.
 * <p>
 * Based on the implementation from:
 * Data Structures and Algorithms in Java, 6th Edition
 * @author Michael T. Goodrich
 * @author Roberto Tamassia
 * @author Michael H. Goldwasser
 */
public class TreeMap<K, V> extends AbstractSortedMap<K, V> {

    protected LinkedBinaryTree<Entry<K, V>> tree = new LinkedBinaryTree<>();

    public TreeMap() {
        super();
        tree.addRoot(null); // Inicialmente, no hay ninguna entrada, así que el árbol tiene solo un nodo raíz que es una hoja (nulo)
    }

    /**
     * Constructs an empty map using the given comparator to order keys.
     *
     * @param comp comparator defining the order of keys in the map
     */
    public TreeMap(Comparator<K> comp) {
        super(comp);
        tree.addRoot(null); // Inicialmente, no hay ninguna entrada, así que el árbol tiene solo un nodo raíz que es una hoja (nulo)
    }

    /**
     * Returns the number of entries in the map.
     *
     * @return number of entries in the map
     */
    @Override
    public int size() {
        // Dado que las hojas no contienen entradas, el número de entradas es la mitad del número total de nodos menos uno (la raíz)
        return (tree.size() - 1) / 2;
    }

    // Definiciones de métodos auxiliares para manipular el árbol binario.
    // Solo por cuestiones de legibilidad, para no tener que estar llamando a los métodos del árbol.
    protected Position<Entry<K, V>> root() {
        return tree.root();
    }

    protected Position<Entry<K, V>> parent(Position<Entry<K, V>> p) {
        return tree.parent(p);
    }

    protected Position<Entry<K, V>> left(Position<Entry<K, V>> p) {
        return tree.left(p);
    }

    protected Position<Entry<K, V>> right(Position<Entry<K, V>> p) {
        return tree.right(p);
    }

    protected Position<Entry<K, V>> sibling(Position<Entry<K, V>> p) {
        return tree.sibling(p);
    }

    protected boolean isRoot(Position<Entry<K, V>> p) {
        return tree.isRoot(p);
    }

    protected boolean isExternal(Position<Entry<K, V>> p) {
        return tree.isExternal(p);
    }

    protected boolean isInternal(Position<Entry<K, V>> p) {
        return tree.isInternal(p);
    }

    protected void set(Position<Entry<K, V>> p, Entry<K, V> e) {
        tree.set(p, e);
    }

    /**
     * Returns the value associated with the specified key, or null if no such entry exists.
     * <p>
     * Devuelve el valor asociado a la clave especificada, o null si no existe tal entrada.
     *
     * @param key la clave cuya entrada se desea obtener
     * @return el valor asociado a la clave (o null si no existe tal entrada)
     */
    @Override
    public V get(K key) {
        Position<Entry<K, V>> position = treeSearch(root(), key);
        if (isExternal(position)) {
            // No hemos encontrado la clave
            return null;
        } else {
            return position.getElement().getValue();
        }
    }

    /**
     * Associates the given value with the given key. If an entry with the key was already in the map,
     * this replaces the previous value with the new one and returns the old value. Otherwise, a new
     * entry is added and null is returned.
     * <p>
     * Asocia el valor dado con la clave dada. Si ya existía una entrada con dicha clave en el mapa,
     * este método reemplaza el valor anterior por el nuevo y devuelve el valor antiguo.
     * De lo contrario, se añade una nueva entrada y se devuelve null.
     *
     * @param key   clave con la que se debe asociar el valor dado
     * @param value valor que se debe asociar con la clave dada
     * @return el valor antiguo asociado a la clave (o null si no existía tal entrada)
     */
    @Override
    public V put(K key, V value) {
        Entry<K, V> newEntry = new MapEntry<>(key, value);
        Position<Entry<K, V>> position = treeSearch(root(), key);
        if (isExternal(position)) {
            // No hemos encontrado la clave, por lo que debemos insertar una nueva entrada
            expandExternal(position, newEntry);
            return null;
        } else {
            // Guardamos el valor antiguo para devolverlo después
            V old = position.getElement().getValue();
            // La clave ya existe, por lo que simplemente actualizamos el valor
            set(position, newEntry);
            // Devolvemos el valor antiguo
            return old;
        }
    }

    /**
     * Dada una posición que es una hoja (nodo externo), la convierte en un nodo interno
     * que contiene la entrada dada, y añade dos nodos hoja (nulos) como hijos.
     * @param position posición que es una hoja (nodo externo)
     * @param entry entrada que se debe almacenar en el nuevo nodo interno
     */
    protected void expandExternal(Position<Entry<K, V>> position, Entry<K, V> entry) {
        set(position, entry); // Cambiamos la hoja por el nuevo nodo con la entrada
        // Añadimos las hojas (nodos nulos) como hijos
        tree.addLeft(position, null);
        tree.addRight(position, null);
    }

    /**
     * Removes the entry with the specified key, if present, and returns its associated value.
     * Otherwise, does nothing and returns null.
     * <p>
     * Elimina la entrada que contiene la clave especificada, si está presente, y devuelve su valor asociado.
     * De lo contrario, no hace nada y devuelve null.
     *
     * @param key la clave cuya entrada debe eliminarse del mapa
     * @return el valor asociado a la clave cuya entrada se ha eliminado (o null si no existía tal entrada)
     */
    @Override
    public V remove(K key) {
        Position<Entry<K, V>> position = treeSearch(root(), key);
        if (isExternal(position)) {
            // No hemos encontrado la clave
            return null;
        } else {
            V old = position.getElement().getValue();
            if (isInternal(left(position)) && isInternal(right(position))) {
                // El nodo a eliminar tiene dos hijos internos.
                // Buscamos el sucesor (máximo del subárbol izquierdo) e intercambiamos su entrada con la del nodo a eliminar.
                Position<Entry<K, V>> replacement = treeMax(left(position));
                set(position, replacement.getElement());
                // Ahora, basta con eliminar el nodo que contenía el sucesor.
                // Dado que el máximo de cualquier subárbol es el final de una rama,
                // tendrá a lo sumo un hijo interno.
                position = replacement;
            }
            // Llegados a este punto, el nodo a eliminar tiene a lo sumo un hijo interno.
            Position<Entry<K, V>> leaf = (isExternal(left(position)) ? left(position) : right(position));
            // Eliminamos el nodo hoja
            tree.remove(leaf);
            // Eliminamos el nodo que contiene la entrada.
            // Dado que solo tiene un hijo (el hermano de la hoja, ya que la hoja la hemos eliminado),
            // este lo sustituye automáticamente.
            // (Véase el método remove de LinkedBinaryTree.)
            tree.remove(position);
            return old;
        }
    }

    /**
     * Returns the position in position's subtree having the given key (or else the terminal leaf).
     * <p>
     * Busca recursivamente la clave en el subárbol cuya raíz es la posición dada.
     * Devuelve la posición que contiene la clave,
     * o la última posición alcanzada durante la búsqueda (una hoja) si no se encuentra la clave.
     *
     * @param key      clave a buscar
     * @param position posición que sirve como raíz del subárbol donde buscar
     * @return Position que contiene una entrada con la clave dada (o una hoja si no se encuentra la clave)
     */
    protected Position<Entry<K, V>> treeSearch(Position<Entry<K, V>> position, K key) {
        if (isExternal(position)) {
            // Hemos llegado a una hoja. Por lo tanto, no hemos encontrado la clave.
            // Devolvemos la posición de la hoja.
            // Al ser una hoja, la función que ha llamado a este método sabrá que no hemos encontrado la clave.
            return position;
        }
        int comparison = compare(key, position.getElement().getKey());
        if (comparison == 0) {
            // Hemos encontrado la clave en el nodo actual.
            return position;
        } else if (comparison < 0) {
            return treeSearch(left(position), key);
        } else {
            return treeSearch(right(position), key);
        }
    }

    /**
     * Returns the entry having the least key (or null if map is empty).
     * <p>
     * Devuelve la entrada con la clave mínima (o null si el mapa está vacío).
     *
     * @return entrada con la clave mínima (o null si el mapa está vacío)
     */
    @Override
    public Entry<K, V> firstEntry() {
        if (this.isEmpty()) return null;
        return this.treeMin(this.root()).getElement();
    }

    /**
     * Returns the entry having the greatest key (or null if map is empty).
     * <p>
     * Devuelve la entrada con la clave máxima (o null si el mapa está vacío).
     *
     * @return entrada con la clave máxima (o null si el mapa está vacío)
     */
    @Override
    public Entry<K, V> lastEntry() {
        if (this.isEmpty()) return null;
        return this.treeMax(this.root()).getElement();
    }

    /**
     * Returns position with the minimal key in the subtree rooted at Position position.
     * <p>
     * Busca el nodo con la clave mínima en el subárbol cuya raíz es la posición dada.
     *
     * @param position a Position of the tree serving as root of a subtree
     * @return Position with minimal key in subtree
     */
    protected Position<Entry<K, V>> treeMin(Position<Entry<K, V>> position) {
        Position<Entry<K, V>> currentPosition = position;
        while (isInternal(currentPosition)) {
            // Caminamos siempre por la izquierda hasta llegar al final de la rama
            currentPosition = left(currentPosition);
        }
        // Hemos llegado a una hoja. Por lo tanto, el nodo con la clave mínima es el padre de esta hoja.
        return parent(currentPosition);
    }

    /**
     * Returns the position with the maximum key in the subtree rooted at position.
     * <p>
     * Busca el nodo con la clave máxima en el subárbol cuya raíz es la posición dada.
     *
     * @param position a Position of the tree serving as root of a subtree
     * @return Position with maximum key in subtree
     */
    protected Position<Entry<K, V>> treeMax(Position<Entry<K, V>> position) {
        Position<Entry<K, V>> currentPosition = position;
        while (isInternal(currentPosition)) {
            // Caminamos siempre por la izquierda hasta llegar al final de la rama
            currentPosition = right(currentPosition);
        }
        // Hemos llegado a una hoja. Por lo tanto, el nodo con la clave mínima es el padre de esta hoja.
        return parent(currentPosition);
    }

    /**
     * Returns the entry with least key greater than or equal to given key
     * (or null if no such key exists).
     * <p>
     * Devuelve la entrada con la clave especificada.
     * Si no existe tal clave, devuelve la entrada con la clave mínima que sea mayor que la clave especificada.
     * Si no hay ninguna clave mayor o igual que la dada, devuelve null.
     *
     * @return entrada con la clave mínima mayor o igual que la dada (o null si no existe tal entrada)
     */
    @Override
    public Entry<K, V> ceilingEntry(K key) {
        Position<Entry<K, V>> position = ceilingEntryPosition(key);
        if (position != null) {
            return position.getElement();
        } else {
            return null;
        }
    }

    private Position<Entry<K, V>> ceilingEntryPosition(K key) {
        Position<Entry<K, V>> position = treeSearch(root(), key);
        if (isInternal(position)) {
            // Hemos encontrado la entrada exacta
            return position;
        }
        // No hemos encontrado la clave, por lo que buscamos la clave mínima mayor que la dada
        return ancestroPosition(position, false);
    }

    /**
     * Returns the entry with greatest key less than or equal to given key
     * (or null if no such key exists).
     * <p>
     * Devuelve la entrada con la clave especificada.
     * Si no existe tal clave, devuelve la entrada con la clave máxima que sea menor que la clave especificada.
     * Si no hay ninguna clave menor o igual que la dada, devuelve null.
     *
     * @return entrada con la clave máxima menor o igual que la dada (o null si no existe tal entrada)
     */
    @Override
    public Entry<K, V> floorEntry(K key) {
        Position<Entry<K, V>> position = treeSearch(root(), key);
        if (isInternal(position)) {
            // Hemos encontrado la entrada exacta
            return position.getElement();
        }
        // No hemos encontrado la clave, por lo que buscamos la clave máxima menor que la dada
        return ancestro(position, true);
    }

    /**
     * Returns the entry with the greatest key strictly less than given key
     * (or null if no such key exists).
     * <p>
     * Devuelve la entrada con la clave máxima estrictamente menor que la clave dada.
     * Es decir, devuelve la entrada con la clave más grande que sea menor que la clave especificada (el predecesor).
     * Si no existe tal clave, devuelve null.
     * <p>
     * Nótese que, a diferencia del método <code>floorEntry</code>, aquí la clave debe ser estrictamente menor.
     * Si la clave está en el mapa, no se devuelve la entrada que contiene la clave.
     *
     * @return entrada con la clave máxima estrictamente menor que la dada (o null si no existe tal entrada)
     */
    @Override
    public Entry<K, V> previousEntry(K key) {
        Position<Entry<K, V>> position = treeSearch(root(), key);
        if (isInternal(position) && isInternal(left(position))) return treeMax(left(position)).getElement();
        return ancestro(position, true);
    }

    /**
     * Returns the entry with least key strictly greater than given key
     * (or null if no such key exists).
     * <p>
     * Devuelve la entrada con la clave mínima estrictamente mayor que la clave dada.
     * Es decir, devuelve la entrada con la clave más pequeña que sea mayor que la clave especificada (el sucesor).
     * Si no existe tal clave, devuelve null.
     * <p>
     * Nótese que, a diferencia del método <code>ceilingEntry</code>, aquí la clave debe ser estrictamente mayor.
     * Si la clave está en el mapa, no se devuelve la entrada que contiene la clave.
     *
     * @return entrada con la clave mínima estrictamente mayor que la dada (o null si no existe tal entrada)
     */
    @Override
    public Entry<K, V> nextEntry(K key) {
        Position<Entry<K, V>> position = treeSearch(root(), key);
        Position<Entry<K, V>> next = nextPosition(position);
        if (next != null) {
            return next.getElement();
        } else {
            return null;
        }
    }

    private Position<Entry<K, V>> nextPosition(Position<Entry<K, V>> position) {
        if (isInternal(position) && isInternal(right(position))) return treeMin(right(position));
        return ancestroPosition(position, false);
    }

    /**
     * Returns entry of position's nearest ancestor with key that is smaller/larger than position.
     * Returns null if no such ancestor exists.
     * <p>
     * Si <code>smaller</code> es <code>true</code>, devuelve el primer ancestro (padre, abuelo, etc.)
     * que tiene una clave menor que la de la posición.
     * Si <code>smaller</code> es <code>false</code>, devuelve el primer ancestro (padre, abuelo, etc.)
     * que tiene una clave mayor que dicha clave.
     */
    protected Entry<K, V> ancestro(Position<Entry<K, V>> position, boolean smaller) {
        Position<Entry<K, V>> positionFound = ancestroPosition(position, smaller);
        if (positionFound != null) {
            return positionFound.getElement();
        } else {
            return null;
        }
    }

    private Position<Entry<K, V>> ancestroPosition(Position<Entry<K, V>> position, boolean smaller) {
        while (!isRoot(position)) {
            if (smaller == (position == right(parent(position)))) return parent(position);
            else position = parent(position);
        }
        return null;
    }

    /**
     * Returns an iterable containing all entries with keys in the range from
     * <code>fromKey</code> inclusive to <code>toKey</code> exclusive.
     * <p>
     * Devuelve un iterable que contiene todas las entradas con claves en el rango desde
     * <code>fromKey</code> inclusive hasta <code>toKey</code> exclusiva.
     * La diferencia de este método con subMap es que este método realiza una instantánea.
     * Es decir, el iterable devuelto contiene todas las entradas en el rango especificado
     * en el momento en que se llama a este método, y no se ve afectado
     * por modificaciones posteriores al mapa.
     *
     * @return Iterable con todas las entradas en el rango especificado
     */
    public Iterable<Entry<K, V>> subMapSnapshot(K fromKey, K toKey) {
        List<Entry<K, V>> buffer = new LinkedList<>();
        if (compare(fromKey, toKey) < 0) // Nos aseguramos de que el rango es válido, es decir, fromKey < toKey
            subMapRecursivo(fromKey, toKey, root(), buffer);
        return buffer;
    }

    private void subMapRecursivo(K fromKey, K toKey, Position<Entry<K, V>> position, List<Entry<K, V>> buffer) {
        if (isInternal(position)) {
            // Si es una hoja, ya no hay más que mirar
            if (compare(position.getElement(), fromKey) < 0) {
                // La clave de la posición actual es menor que fromKey,
                // por lo que las entradas relevantes están a la derecha de esta posición
                subMapRecursivo(fromKey, toKey, right(position), buffer);
            } else {
                // La clave de la posición actual es mayor o igual que fromKey,
                // por lo que las entradas relevantes pueden estar tanto a la izquierda como a la derecha.
                // Primero consideramos el subárbol izquierdo, que contendrá valores menores que la posición actual
                // y puede que mayores o iguales que fromKey.
                subMapRecursivo(fromKey, toKey, left(position), buffer);
                // A continuación, si la clave de la posición actual es menor que toKey, entonces está dentro del rango.
                // Por lo tanto, añadimos la entrada al buffer y consideramos el subárbol derecho,
                // que contendrá entradas con claves mayores que fromKey y puede que menores que toKey.
                if (compare(position.getElement(), toKey) < 0) {
                    buffer.add(position.getElement());
                    subMapRecursivo(fromKey, toKey, right(position), buffer);
                }
            }
        }
    }

    /**
     * Returns an iterable containing all entries with keys in the range from
     * <code>fromKey</code> inclusive to <code>toKey</code> exclusive.
     * <p>
     * Devuelve un iterable que contiene todas las entradas con claves en el rango desde
     * <code>fromKey</code> inclusive hasta <code>toKey</code> exclusiva.
     *
     * @return Iterable con todas las entradas en el rango especificado
     */
    @Override
    public Iterable<Entry<K, V>> subMap(K fromKey, K toKey) {
        return new subMapIterable(fromKey, toKey);
    }

    /**
     * Returns an iterable collection of all key-value entries of the map.
     * <p>
     * Devuelve un objeto iterable de todas las entradas (pares clave-valor) del mapa.
     *
     * @return objeto iterable de todas las entradas del mapa
     */
    @Override
    public Iterable<Entry<K, V>> entrySet() {
        return tree.elements();
    }

    /**
     * Returns an iterable collection of all key-value entries of the map.
     * <p>
     * Devuelve un objeto iterable de todas las entradas (pares clave-valor) del mapa.
     *
     * @return objeto iterable de todas las entradas del mapa
     */
    public Iterable<Entry<K, V>> entrySetSnapshot() {
        List<Entry<K, V>> buffer = new LinkedList<>();
        for (Position<Entry<K, V>> position : tree.inorder())
            if (isInternal(position)) buffer.add(position.getElement());
        return buffer;
    }

    private class subMapIterable implements Iterable<Entry<K, V>> {
        private final K fromKey;
        private final K toKey;

        public subMapIterable(K fromKey, K toKey) {
            this.fromKey = fromKey;
            this.toKey = toKey;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new subMapIterator(fromKey, toKey);
        }
    }

    private class subMapIterator implements Iterator<Entry<K, V>> {
        private final K toKey;
        private Position<Entry<K, V>> nextPosition;

        public subMapIterator(K fromKey, K toKey) {
            this.toKey = toKey;
            this.nextPosition = ceilingEntryPosition(fromKey);
        }

        @Override
        public boolean hasNext() {
            return nextPosition != null && compare(nextPosition.getElement().getKey(), toKey) < 0;
        }

        @Override
        public Entry<K, V> next() {
            Position<Entry<K, V>> currentPosition = nextPosition;
            nextPosition = nextPosition(currentPosition);
            return currentPosition.getElement();
        }
    }

    // remainder of class is for debug purposes only
    /**
     * Prints textual representation of tree structure (for debug purpose only).
     */
    protected StringBuilder dump() {
        StringBuilder sb = new StringBuilder();
        return dumpRecurse(root(), 0, sb);
    }

    /**
     * This exists for debugging only
     */
    private StringBuilder dumpRecurse(Position<Entry<K, V>> p, int depth, StringBuilder sb) {
        String indent = (depth == 0 ? "" : String.format("%" + (2 * depth) + "s", ""));
        if (isExternal(p)) sb.append(indent).append("leaf\n");
        else {
            sb.append(indent).append(p.getElement()).append("\n");
            dumpRecurse(left(p), depth + 1, sb);
            dumpRecurse(right(p), depth + 1, sb);
        }
        return sb;
    }

}
