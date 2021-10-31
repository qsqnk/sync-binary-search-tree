package trees

import nodes.LockableNode
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

/**
 * Class represents binary search tree with fine-grained synchronization
 *
 */
class SynchronizedBinarySearchTree<K : Comparable<K>, V> : IBinarySearchTree<K, V> {

    private val treeLock = ReentrantLock()

    private val _size = AtomicInteger(0)

    val size get() = _size.get()

    internal var root: LockableNode<K, V>? = null

    override fun get(key: K): V? {
        treeLock.lock()
        root?.lock()
        try {
            root ?: return null
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
       treeLock.lock()
        root?.lock()
        try {
            root?.let { root ->
                if (key == root.key) {
                    root.value = value
                    root.unlock()
                    return
                }
            } ?: run {
                root = LockableNode(key, value).also { _size.incrementAndGet() }
                return
            }
        } finally {
            treeLock.unlock()
        }

        root?.let {
            insert(key, value, it)
        }
    }

    override fun remove(key: K): Boolean {
        treeLock.lock()
        root?.lock()
        try {
            root?.let {
                if (key == it.key && it.isLeaf) {
                    this.root = null
                    _size.decrementAndGet()
                    return true
                }
            } ?: return false
        } finally {
            treeLock.unlock()
        }

        val toBeRemoved = root?.let { findNode(key, it) }
        return toBeRemoved?.let {
            removeNode(it)
        } ?: false
    }

    /**
     * Inserts node with key [key], value [value] into tree with root [rootOfSubtree]
     *
     * Assumed that [rootOfSubtree] is locked
     */
    private tailrec fun insert(key: K, value: V, rootOfSubtree: LockableNode<K, V>): Unit = when (key) {
        rootOfSubtree.key -> {
            rootOfSubtree.value = value
            rootOfSubtree.unlock()
        }
        else -> {
            val nextRoot = if (key < rootOfSubtree.key) rootOfSubtree.left else rootOfSubtree.right
            if (nextRoot != null) {
                nextRoot.lock()
                rootOfSubtree.unlock()
                insert(key, value, nextRoot)
            } else {
                val newNode = LockableNode(key, value).apply { parent = rootOfSubtree }
                if (key < rootOfSubtree.key) rootOfSubtree.left = newNode
                else rootOfSubtree.right = newNode
                _size.incrementAndGet()
                rootOfSubtree.unlock()
            }
        }
    }

    /**
     * Removes [toBeRemoved] from tree
     *
     * Assumed that [toBeRemoved] and its parent are locked
     */
    private fun removeNode(toBeRemoved: LockableNode<K, V>): Boolean {
        val parent = toBeRemoved.parent

        if (toBeRemoved.isLeaf) {
            when (toBeRemoved) {
                parent?.left -> parent.left = null
                parent?.right -> parent.right = null
            }
            _size.decrementAndGet()
            parent?.unlock()
            return true
        }

        parent?.unlock()

        toBeRemoved.right?.let { toBeRemovedRight ->
            toBeRemovedRight.lock()

            if (toBeRemovedRight.left == null) {
                val newChild = toBeRemovedRight.right?.apply { this.parent = toBeRemoved }?.also { it.lock() }

                toBeRemoved.key = toBeRemovedRight.key
                toBeRemoved.value = toBeRemovedRight.value
                toBeRemoved.right = newChild.also { _size.decrementAndGet() }

                newChild?.unlock()
                toBeRemoved.unlock()
                return true
            }


            val successor = successor(toBeRemoved) ?: return@let
            val successorParent = successor.parent
            val newChild = successor.right?.apply { this.parent = successorParent }?.also { it.lock() }

            toBeRemoved.key = successor.key
            toBeRemoved.value = successor.value

            successorParent?.left = newChild?.also { it.unlock() }
            _size.decrementAndGet()

            successorParent?.unlock()
            toBeRemoved.unlock()
            return true
        }

        toBeRemoved.left?.let { toBeRemovedLeft ->
            toBeRemovedLeft.lock()

            if (toBeRemovedLeft.right == null) {
                val newChild = toBeRemovedLeft.left?.apply { this.parent = toBeRemoved }?.also { it.lock() }

                toBeRemoved.key = toBeRemovedLeft.key
                toBeRemoved.value = toBeRemovedLeft.value
                toBeRemoved.left = newChild.also { _size.decrementAndGet() }

                newChild?.unlock()
                toBeRemoved.unlock()
                return true
            }

            val predecessor = predecessor(toBeRemoved) ?: return@let
            val predecessorParent = predecessor.parent
            val newChild = predecessor.left?.apply { this.parent = predecessorParent }?.also { it.lock() }

            toBeRemoved.key = predecessor.key
            toBeRemoved.value = predecessor.value

            predecessorParent?.right = newChild?.also { it.unlock() }
            _size.decrementAndGet()

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
    private fun successor(node: LockableNode<K, V>): LockableNode<K, V>? {
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

    /**
     * Finds predecessor
     * If predecessor is found predecessor and its parent are kept blocked
     *
     * @return predecessor or null
     */
    private fun predecessor(node: LockableNode<K, V>): LockableNode<K, V>? {
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
     * Finds node with key [key]
     * Assumed that [rootOfSubtree] is locked
     * If node is found keeps it and its parent locked
     *
     * @return if node is found node otherwise null
     */

    private tailrec fun findNode(key: K, rootOfSubtree: LockableNode<K, V>): LockableNode<K, V>? = when (key) {
        rootOfSubtree.key -> rootOfSubtree
        else -> {
            val nextRoot = if (key < rootOfSubtree.key) rootOfSubtree.left else rootOfSubtree.right
            if (nextRoot != null) {
                nextRoot.lock()
                if (key == nextRoot.key) {
                    nextRoot
                } else {
                    rootOfSubtree.unlock()
                    findNode(key, nextRoot)
                }
            } else {
                rootOfSubtree.unlock()
                null
            }
        }
    }
}