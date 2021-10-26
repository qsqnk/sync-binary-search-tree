internal data class Node<K : Comparable<K>, V>(
    internal var key: K,
    internal var value: V
) {

    internal var left: Node<K, V>? = null

    internal var right: Node<K, V>? = null

    internal var parent: Node<K, V>? = null

    internal val isLeaf get() = left == null && right == null
}