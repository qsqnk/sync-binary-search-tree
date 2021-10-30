package nodes

internal class Node<K : Comparable<K>, V>(
    key: K,
    value: V,
) : AbstractNode<K, V, Node<K, V>>(key, value) {

    override var left: Node<K, V>? = null

    override var right: Node<K, V>? = null

    override var parent: Node<K, V>? = null

}