package nodes

import java.util.concurrent.locks.ReentrantLock

internal class LockableNode<K : Comparable<K>, V>(
    key: K,
    value: V,
) : AbstractNode<K, V, LockableNode<K, V>>(key, value) {

    internal val lock = ReentrantLock()

    override var left: LockableNode<K, V>? = null

    override var right: LockableNode<K, V>? = null

    override var parent: LockableNode<K, V>? = null

    internal fun lock() {
        lock.lock()
    }

    internal fun unlock() {
        lock.unlock()
    }
}