package trees

import nodes.Node

class BinarySearchTree<K : Comparable<K>, V> : IBinarySearchTree<K, V> {

    internal var root: Node<K, V>? = null

    override fun get(key: K): V? = findNode(key)?.value

    override fun remove(key: K): Boolean = findNode(key)?.let { removeNode(it) } ?: false

    override fun set(key: K, value: V) {
        if (root == null) {
            root = Node(key, value)
            return
        }

        var (parent, cur) = root?.parent to root

        while (cur != null && cur.key != key) {
            parent = cur
            cur = if (key < cur.key) cur.left else cur.right
        }

        cur?.let {
            cur.value = value
            return
        }

        parent?.let { p ->
            val newNode = Node(key, value).apply { this.parent = p }
            if (key < p.key) p.left = newNode
            else p.right = newNode
        }
    }

    private fun findNode(key: K): Node<K, V>? {
        var cur = root

        while (cur != null && cur.key != key) {
            cur = if (key < cur.key) cur.left else cur.right
        }

        return cur
    }

    private fun removeNode(node: Node<K, V>): Boolean {
        val parent = node.parent

        return when {
            node.isLeaf -> {
                when (node) {
                    root -> root = null
                    parent?.left -> parent.left = null
                    parent?.right -> parent.right = null
                }
                true
            }
            node.left == null || node.right == null -> {
                val newChild = (node.left ?: node.right).apply { this?.parent = parent }
                when (node) {
                    root -> root = newChild
                    parent?.left -> parent.left = newChild
                    parent?.right -> parent.right = newChild
                }
                true
            }
            else -> {
                next(node)?.let { next ->
                    removeNode(next)
                    node.key = next.key
                    node.value = next.value
                    true
                } ?: false
            }
        }
    }

    private fun next(node: Node<K, V>): Node<K, V>? {
        node.right?.let { return minInSubtree(it) }

        var next: Node<K, V>? = node
        while (next != null && next == next.parent?.right) {
            next = next.parent
        }
        return next?.parent
    }

    private tailrec fun minInSubtree(subTreeRoot: Node<K, V>?): Node<K, V>? = when (subTreeRoot?.left) {
        null -> subTreeRoot
        else -> minInSubtree(subTreeRoot.left)
    }
}
