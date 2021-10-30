import java.util.concurrent.locks.ReentrantLock

internal data class SynchronizedNode<K : Comparable<K>, V>(
    internal var key: K,
    internal var value: V
) {
    internal val lock = ReentrantLock()

    internal var left: SynchronizedNode<K, V>? = null

    internal var right: SynchronizedNode<K, V>? = null

    internal var parent: SynchronizedNode<K, V>? = null

    internal val isLeaf get() = left == null && right == null

    internal fun lock() {
        lock.lock()
        //println("Node with key: $key is unlocked by thread ${Thread.currentThread().id}")
    }

    internal fun unlock() {
        lock.unlock()
        //println("Node with key: $key is locked by thread ${Thread.currentThread().id}")
    }
}