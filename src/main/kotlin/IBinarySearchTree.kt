interface IBinarySearchTree <K: Comparable<K>, V> {

    operator fun set(key: K, value: V)

    operator fun get(key: K): V?

    fun remove(key: K): Boolean
}