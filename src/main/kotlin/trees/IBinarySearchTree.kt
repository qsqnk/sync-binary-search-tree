package trees

/**
 * @param [K] type of keys stored
 * @param [V] type of values stored
 */
interface IBinarySearchTree<K : Comparable<K>, V> {

    /**
     * Adds node with key [key], value [value]
     *
     * Allows to use the index operator for storing values in a binary search tree
     */
    operator fun set(key: K, value: V)

    /**
     * Finds node with key [key]
     *
     * Allows to use the index operator for getting value by key
     *
     * @return if the tree has vertex with key [key] its value otherwise null
     */
    operator fun get(key: K): V?

    /**
     * Removes node with key [key] from tree
     *
     * @return true if node was in tree otherwise false
     */
    fun remove(key: K): Boolean

}