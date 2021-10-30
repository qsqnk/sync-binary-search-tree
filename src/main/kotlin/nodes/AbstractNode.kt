package nodes

internal abstract class AbstractNode<K, V, N : AbstractNode<K, V, N>>(
    internal var key: K,
    internal var value: V
) {

    internal abstract var left: N?

    internal abstract var right: N?

    internal abstract var parent: N?

    internal val isLeaf get() = left == null && right == null
}