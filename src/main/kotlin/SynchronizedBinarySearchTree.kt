import java.util.concurrent.locks.ReentrantLock

class SynchronizedBinarySearchTree<K : Comparable<K>, V> : IBinarySearchTree<K, V> {

    private val treeLock = ReentrantLock()

    internal var root: SynchronizedNode<K, V>? = null

    override fun get(key: K): V? = findNode(key, unlockParentAtEnd = true)?.value

    override fun set(key: K, value: V) {

        var parent: SynchronizedNode<K, V>?
        var cur: SynchronizedNode<K, V>?

        treeLock.lock()
        try {
            parent = root?.also { it.lock() } ?: run {
                root = SynchronizedNode(key, value)
                return
            }
            cur = if (key < parent.key) parent.left else parent.right
        } finally {
            treeLock.unlock()
        }

        cur?.lock()

        try {
            while (cur != null && key != cur.key) {
                parent?.unlock()
                parent = cur
                cur = if (key < cur.key) cur.left else cur.right
                cur?.lock()
            }

            cur?.let {
                it.value = value
                return
            }

            parent?.let { p ->
                val newChild = SynchronizedNode(key, value).apply { this.parent = p }
                if (key < p.key) p.left = newChild
                else p.right = newChild
            }

        } finally {
            cur?.unlock()
            parent?.unlock()
        }
    }


    override fun remove(key: K): Boolean =
        findNode(key, unlockParentAtEnd = false)?.let { removeNodeWithLockedParent(it) } ?: false

    private fun removeNodeWithLockedParent(node: SynchronizedNode<K, V>): Boolean {
        val parent = node.parent //locked

        when {
            node.isLeaf -> {
                when (node) {
                    root -> root = null
                    parent?.left -> parent.left = null
                    parent?.right -> parent.right = null
                }
                parent?.unlock()
            }
            node.left == null || node.right == null -> {
                val newChild = (node.left ?: node.right).apply { this?.parent = parent }
                when (node) {
                    root -> root = newChild
                    parent?.left -> parent.left = newChild
                    parent?.right -> parent.right = newChild
                }
                parent?.unlock()
            }
            else -> {
                val next = next(node.key, unlockParentAtEnd = false)
                if (next == null) {
                    parent?.unlock()
                    return false
                }
                if (parent != next.parent) parent?.unlock()
                removeNodeWithLockedParent(next)
                node.key = next.key
                node.value = next.value
            }
        }

        return true
    }

    private fun findNode(
        key: K,
        unlockParentAtEnd: Boolean
    ): SynchronizedNode<K, V>? {
        var parent: SynchronizedNode<K, V>?
        var cur: SynchronizedNode<K, V>?

        treeLock.lock()
        try {
            parent = root?.also {
                if (it.key == key) return it
                it.lock()
            } ?: return null
            cur = if (key < parent.key) parent.left else parent.right
        } finally {
            treeLock.unlock()
        }

        cur?.lock()

        try {
            while (cur != null && key != cur.key) {
                parent?.unlock()
                parent = cur
                cur = if (key < cur.key) cur.left else cur.right
                cur?.lock()
            }
            return cur

        } finally {
            cur?.also {
                it.unlock()
                if (unlockParentAtEnd) parent?.unlock()
            } ?: parent?.unlock()
        }
    }

    private fun next(
        key: K,
        unlockParentAtEnd: Boolean
    ): SynchronizedNode<K, V>? {

        var lastVisitedNodeWithGreaterKeyParent: SynchronizedNode<K, V>? = null
        var lastVisitedNodeWithGreaterKey: SynchronizedNode<K, V>? = null
        var parent: SynchronizedNode<K, V>?
        var cur: SynchronizedNode<K, V>?

        treeLock.lock()
        try {
            parent = root?.also {
                if (it.key > key) lastVisitedNodeWithGreaterKey = it
                it.lock()
            } ?: return null
            cur = if (key < parent.key) parent.left else parent.right
        } finally {
            treeLock.unlock()
        }

        cur?.lock()

        try {
            while (cur != null) {
                val newParent = cur
                if (key < cur.key) {
                    val old = lastVisitedNodeWithGreaterKeyParent
                    lastVisitedNodeWithGreaterKeyParent = newParent.also { it.lock() }
                    old?.unlock()
                    lastVisitedNodeWithGreaterKey = cur
                    cur = cur.left
                } else {
                    cur = cur.right
                }
                if (parent != lastVisitedNodeWithGreaterKeyParent) parent?.unlock()
                parent = newParent
                cur?.lock()
            }
            return lastVisitedNodeWithGreaterKey

        } finally {
            cur?.unlock()
            if (parent != lastVisitedNodeWithGreaterKeyParent) parent?.unlock()
            if (unlockParentAtEnd) lastVisitedNodeWithGreaterKeyParent?.unlock()
        }
    }
}