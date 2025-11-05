package es.urjc.grafo.EDA.searchtree;

import es.urjc.grafo.EDA.util.Entry;
import es.urjc.grafo.EDA.util.Position;

/**
 * An implementation of a sorted map using an AVL tree.
 *
 * @param <K> The key type (keys must be unique and comparable)
 * @param <V> The value type
 */
public class AVLTreeMap<K, V> extends AbstractBalanceableTreeMap<K, V> {

    /**
     * Devuelve la altura de una posición del árbol.
     */
    protected int height(Position<Entry<K, V>> position) {
        return tree.getAux(position);
    }

    /**
     * Recalcula la altura de un nodo position, que será el máximo de las alturas de su subárbol izquierdo y
     * su subárbol derecho + 1.
     * Por ejemplo, si el subárbol izquierdo de position tiene 3 niveles y el subárbol derecho de position
     * tiene 2 niveles, entonces la altura de position es 4.
     *
     */
    protected void recomputeHeight(Position<Entry<K, V>> position) {
        tree.setAux(position, 1 + Math.max(height(left(position)), height(right(position))));
    }

    /**
     * Returns whether a position has balance factor between -1 and 1 inclusive.
     */
    protected boolean isBalanced(Position<Entry<K, V>> position) {
        return Math.abs(height(left(position)) - height(right(position))) <= 1;
    }

    /**
     * Devuelve el hijo más alto de una posición position. Si ambos hijos tienen la misma altura,
     * devuelve el hijo que esté alineado con la posición padre (es decir, si position es hijo izquierdo de su padre,
     * devuelve el hijo izquierdo de position; si position es hijo derecho de su padre, devuelve el hijo derecho de position).
     *
     * @param position Position cuyo hijo más alto se desea conocer
     * @return Hijo más alto de position
     */
    protected Position<Entry<K, V>> tallerChild(Position<Entry<K, V>> position) {
        if (height(left(position)) > height(right(position))) return left(position);
        if (height(left(position)) < height(right(position))) return right(position);
        // Devuelve el hijo alineado con el padre en caso de empate
        if (isRoot(position)) return left(position);                 // choice is irrelevant
        if (position == left(parent(position))) return left(position);      // return aligned child
        else return right(position);
    }

    /**
     * Utility used to rebalance after an insert or removal operation. This traverses the
     * path upward from position, performing a trinode restructuring when imbalance is found,
     * continuing until balance is restored.
     * <p>
     * Dada una posición position, reestructura todas las posiciones desbalanceadas desde position hasta la raíz.
     * Importante: cada nodo tiene asociada una altura. Véase el método height() de esta clase,
     * que devuelve el miembro aux del nodo. Cuando se realiza una operación de reestructuración, es importante
     * recalcular la altura de los nodos afectados (los hijos del nodo reestructurado y el propio nodo reestructurado).
     * Esto se puede hacer utilizando el método recomputeHeight().
     *
     * @param position Position from which to start rebalancing
     */
    protected void rebalance(Position<Entry<K, V>> position) {
        int oldHeight, newHeight;
        do {
            oldHeight = height(position);                     // not yet recalculated if internal
            if (!isBalanced(position)) {                      // imbalance detected
                // perform trinode restructuring, setting position to resulting root,
                // and recompute new local heights after the restructuring
                position = restructure(tallerChild(tallerChild(position)));
                recomputeHeight(left(position));
                recomputeHeight(right(position));
            }
            recomputeHeight(position);
            newHeight = height(position);  // If newHeight is not equal to oldHeight, then the height of the tree has changed,
            // and we must continue rebalancing upwards
            position = parent(position);
        } while (oldHeight != newHeight && position != null);  // Stop when we reach the root of the tree or the height of the tree has not changed
    }

    /**
     * AVLTreeMap rebalancing hook that is called after an insertion.
     */
    protected void rebalanceInsert(Position<Entry<K, V>> position) {
        rebalance(position);
    }

    /**
     * AVLTreeMap rebalancing hook that is called after a deletion.
     */
    protected void rebalanceDelete(Position<Entry<K, V>> position) {
        if (!isRoot(position)) rebalance(parent(position));
    }

    // Remainder of this class is for debugging and testing purposes only

    /**
     * Ensure that current tree structure is valid AVL (for debug use only).
     */
    private boolean sanityCheck() {
        for (Position<Entry<K, V>> p : tree.positions()) {
            if (isInternal(p)) {
                if (p.getElement() == null) System.out.println("VIOLATION: Internal node has null entry");
                else if (height(p) != 1 + Math.max(height(left(p)), height(right(p)))) {
                    System.out.println("VIOLATION: AVL unbalanced node with key " + p.getElement().getKey());
                    System.out.println(dump());
                    return false;
                } else if (Math.abs(recalculateHeight(left(p)) - recalculateHeight(right(p))) > 1) {
                    System.out.println("VIOLATION: AVL unbalanced node with key " + p.getElement().getKey());
                    System.out.println(dump());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Recalculate the height of a node position by recursively calculating the height of its children.
     * Does not consult the property aux of the nodes involved, but calculates height from scratch.
     * For debugging and testing purposes only.
     *
     * @param position Position to check
     * @return height of the node position
     */
    private int recalculateHeight(Position<Entry<K, V>> position) {
        if (isExternal(position)) return 0;
        return 1 + Math.max(recalculateHeight(left(position)), recalculateHeight(right(position)));
    }
}
