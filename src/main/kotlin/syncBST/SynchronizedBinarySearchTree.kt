package syncBST

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

/**
 * Class represents binary search tree with fine-grained synchronization
 * Hand-over-hand locking is used while traversing
 * Root lock can be obtained only after global tree lock
 *
 * @param [K] type of keys stored
 * @param [V] type of values stored
 *
 */
class SynchronizedBinarySearchTree<K : Comparable<K>, V> : IBinarySearchTree<K, V> {

    private val treeLock = ReentrantLock()
    private val _size = AtomicInteger(0)
    internal var root: LockableNode<K, V>? = null

    val size get() = _size.get()

    override fun get(key: K): V? {
        lockAndProcessRoot { root -> root ?: return null }
        return root?.let {
            val (parent, node) = findNodeAndParent(key, it)
            val value = node?.value
            node?.unlock()
            parent?.unlock()
            value
        }
    }

    override fun set(key: K, value: V) {
        lockAndProcessRoot { root ->
            root ?: run {
                this.root = LockableNode(key, value).also { _size.incrementAndGet() }
                return
            }
        }
        insertNonRoot(key, value)
    }

    override fun remove(key: K): Boolean {
        lockAndProcessRoot { root ->
            root ?: return false
            val (left, right) = root.left to root.right
            if (key == root.key && right == null) {
                this@SynchronizedBinarySearchTree.root = left?.apply { parent = null }
                _size.decrementAndGet()
                return true
            }
        }
        return root?.let {
            val (parent, toBeRemoved) = findNodeAndParent(key, it)
            toBeRemoved?.let { removeNode(toBeRemoved) } ?: false.also { parent?.unlock() }
        } ?: false
    }

    /**
     * Inserts node with key [key], value [value]
     *
     * Assumed that root is locked and tree is not empty
     */
    private fun insertNonRoot(key: K, value: V) {
        val root = root ?: return
        val (parent, node) = findNodeAndParent(key, root)
        node?.let { it.value = value } ?: run {
            parent ?: return
            val newChild = LockableNode(key, value).apply { this.parent = parent }
            if (key < parent.key) parent.left = newChild else parent.right = newChild
        }
        _size.incrementAndGet()
        node?.unlock()
        parent?.unlock()
    }

    /**
     * Removes [toBeRemoved] from tree
     *
     * Assumed that [toBeRemoved] and its parent are locked
     */
    private fun removeNode(toBeRemoved: LockableNode<K, V>): Boolean {
        val toBeRemovedParent = toBeRemoved.parent
        val (left, right) = toBeRemoved.left to toBeRemoved.right
        right ?: run {
            val newChild = left?.apply { parent = toBeRemovedParent }
            when (toBeRemoved) {
                toBeRemovedParent?.left -> toBeRemovedParent.left = newChild
                toBeRemovedParent?.right -> toBeRemovedParent.right = newChild
            }
            toBeRemovedParent?.unlock()
            return true
        }
        toBeRemovedParent?.unlock()
        right.lock()
        return right.left?.let {
            whenSuccessorIsMostLeftInRightSubtree(toBeRemoved)
        } ?: whenSuccessorIsRightSon(toBeRemoved)
    }

    private fun whenSuccessorIsMostLeftInRightSubtree(toBeRemoved: LockableNode<K, V>): Boolean {
        val right = toBeRemoved.right ?: return false
        var (successorParent, successor) = right to right.left?.apply { lock() }
        while (successor?.left != null) {
            successorParent.unlock()
            successorParent = successor
            successor = successor.left
            successor?.lock()
        }
        successor ?: return false
        val newChild = successor.right?.apply { parent = successorParent }
        with (toBeRemoved) {
            key = successor.key
            value = successor.value
        }
        successorParent.left = newChild.also { _size.decrementAndGet() }
        successorParent.unlock()
        toBeRemoved.unlock()
        return true
    }

    private fun whenSuccessorIsRightSon(toBeRemoved: LockableNode<K, V>): Boolean {
        val rightSon = toBeRemoved.right ?: return false
        val newChild = rightSon.right?.apply { parent = toBeRemoved }
        with(toBeRemoved) {
            key = rightSon.key
            value = rightSon.value
            right = newChild.also { _size.decrementAndGet() }
        }
        toBeRemoved.unlock()
        return true
    }

    /**
     * Finds node with key [key]
     * Assumed that [rootOfSubtree] is locked
     * If node is found keeps it and its parent locked
     *
     * @return pair of parent and node parent
     */

    private tailrec fun findNodeAndParent(
        key: K,
        rootOfSubtree: LockableNode<K, V>
    ): Pair<LockableNode<K, V>?, LockableNode<K, V>?> = when (key) {
        rootOfSubtree.key -> null to rootOfSubtree
        else -> {
            val nextRoot = if (key < rootOfSubtree.key) rootOfSubtree.left else rootOfSubtree.right
            if (nextRoot != null) {
                nextRoot.lock()
                if (key == nextRoot.key) {
                    rootOfSubtree to nextRoot
                } else {
                    rootOfSubtree.unlock()
                    findNodeAndParent(key, nextRoot)
                }
            } else rootOfSubtree to null
        }
    }

    /**
     * Locks root and processes [action]
     * under global tree lock
     *
     */
    private inline fun lockAndProcessRoot(action: (LockableNode<K, V>?) -> Unit) {
        treeLock.lock()
        try {
            root?.lock()
            action(root)
        } finally {
            treeLock.unlock()
        }
    }
}
