import java.util.concurrent.locks.ReentrantLock

class SynchronizedBinarySearchTree<K : Comparable<K>, V> : IBinarySearchTree<K, V> {

    private val treeLock = ReentrantLock()

    internal var root: SynchronizedNode<K, V>? = null

    override fun get(key: K): V? {
        var root: SynchronizedNode<K, V>?

        treeLock.lock()
        try {
            root = this.root?.also { it.lock() } ?: return null
        } finally {
            treeLock.unlock()
        }

        return root?.let {
            findNode(key, it)?.let { foundNode ->
                foundNode.unlock()
                foundNode.parent?.unlock()
                foundNode.value
            }
        }

    }

    override fun set(key: K, value: V) {
        var parent: SynchronizedNode<K, V>?
        var cur: SynchronizedNode<K, V>?

        treeLock.lock()
        try {
            // if root is null put node at root
            parent = root?.also { r ->
                r.lock()
            } ?: run {
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

            // found node's key is equal to node's key to be inserted
            cur?.let {
                it.value = value
                return
            }

            // add new leaf to tree
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


    override fun remove(key: K): Boolean {
        var parent: SynchronizedNode<K, V>?
        var toBeRemoved: SynchronizedNode<K, V>?

        println("start removing node ${key} by thread ${Thread.currentThread().id}")

        treeLock.lock()
        root?.lock()
        try {
            val root = this.root
            if (root != null) {
                if (key == root.key && root.isLeaf) {
                    this.root = null
                    return true
                }
            } else return false

            toBeRemoved = findNode(key, root) ?: return false
            parent = toBeRemoved.parent

            if (toBeRemoved.isLeaf && parent == null) {
                this.root = null
                return true
            }
        } finally {
            treeLock.unlock()
        }

        toBeRemoved ?: return false

        if (toBeRemoved.isLeaf) {
            when (toBeRemoved) {
                parent?.left -> parent.left = null
                parent?.right -> parent.right = null
            }
            parent?.unlock()
            return true
        }

        // in case of removing internal nodes we have no need keep parent locked

        parent?.unlock()

        toBeRemoved.right?.let { toBeRemovedRight ->
            toBeRemovedRight.lock()

            if (toBeRemovedRight.left == null) {
                toBeRemovedRight.lock()

                if (toBeRemovedRight.left == null) {
                    val newChild = toBeRemovedRight.right?.also { it.lock() }
                    newChild?.parent = toBeRemoved

                    toBeRemoved.key = toBeRemovedRight.key
                    toBeRemoved.value = toBeRemovedRight.value
                    toBeRemoved.right = newChild

                    newChild?.unlock()
                    toBeRemoved.unlock()
                    return true
                }
            }

            val successor = successor(toBeRemoved) ?: return@let
            val successorParent = successor.parent
            val newChild = successor.right?.also { it.lock() }
            newChild?.parent = successorParent

            toBeRemoved.key = successor.key
            toBeRemoved.value = successor.value

            successorParent?.left = newChild?.also { it.unlock() }

            successorParent?.unlock()
            toBeRemoved.unlock()
            return true
        }

        toBeRemoved.left?.let { toBeRemovedLeft ->
            toBeRemovedLeft.lock()

            if (toBeRemovedLeft.right == null) {
                val newChild = toBeRemovedLeft.left?.also { it.lock() }
                newChild?.parent = toBeRemoved

                toBeRemoved.key = toBeRemovedLeft.key
                toBeRemoved.value = toBeRemovedLeft.value
                toBeRemoved.left = newChild

                newChild?.unlock()
                toBeRemoved.unlock()
                return true
            }

            val predecessor = predecessor(toBeRemoved) ?: return@let
            val predecessorParent = predecessor.parent
            val newChild = predecessor.left?.also { it.lock() }
            newChild?.parent = predecessorParent

            toBeRemoved.key = predecessor.key
            toBeRemoved.value = predecessor.value

            predecessorParent?.right = newChild?.also { it.unlock() }

            predecessorParent?.unlock()
            toBeRemoved.unlock()
            return true
        }

        return false
    }

    /**
     * Finds successor
     * If successor is found successor and its parent are kept blocked
     *
     * @return successor or null
     */

    private fun successor(node: SynchronizedNode<K, V>): SynchronizedNode<K, V>? {
        var successorParent = node.right
        var successor = successorParent?.left

        successor?.lock()

        while (successor?.left != null) {
            successorParent?.unlock()
            successorParent = successor
            successor = successor.left
            successor?.lock()
        }

        successor ?: successorParent?.unlock()
        return successor
    }


    private fun predecessor(node: SynchronizedNode<K, V>): SynchronizedNode<K, V>? {
        var predecessorParent = node.left
        var predecessor = predecessorParent?.right

        predecessor?.lock()

        while (predecessor?.right != null) {
            predecessorParent?.unlock()
            predecessorParent = predecessor
            predecessor = predecessor.right
            predecessor?.lock()
        }

        predecessor ?: predecessorParent?.unlock()
        return predecessor
    }

    /**
     * Assumed that [rootOfSubtree] is locked
     * Finds node with key [key]
     * If node is found keeps it and its parent locked
     *
     * @return if node is found node otherwise null
     */

    private fun findNode(key: K, rootOfSubtree: SynchronizedNode<K, V>): SynchronizedNode<K, V>? = when {
        key == rootOfSubtree.key -> rootOfSubtree
        key < rootOfSubtree.key -> {
            val l = rootOfSubtree.left
            if (l != null) {
                l.lock()
                if (key == l.key) l
                else {
                    rootOfSubtree.unlock()
                    findNode(key, l)
                }
            } else {
                rootOfSubtree.unlock()
                null
            }
        }
        else -> {
            val r = rootOfSubtree.right
            if (r != null) {
                r.lock()
                if (key == r.key) r
                else {
                    rootOfSubtree.unlock()
                    findNode(key, r)
                }
            } else {
                rootOfSubtree.unlock()
                null
            }
        }
    }
}