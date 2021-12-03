package syncBST

import java.util.concurrent.locks.ReentrantLock

internal class LockableNode<K : Comparable<K>, V>(
    internal var key: K,
    internal var value: V
) {

    private val lock = ReentrantLock()

    internal var left: LockableNode<K, V>? = null

    internal var right: LockableNode<K, V>? = null

    internal var parent: LockableNode<K, V>? = null

    internal fun lock() {
        lock.lock()
    }

    internal fun unlock() {
        lock.unlock()
    }
}