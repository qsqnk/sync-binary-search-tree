import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import trees.SynchronizedBinarySearchTree

internal class SynchronizedBinarySearchTreeTests {

    private lateinit var syncTree: SynchronizedBinarySearchTree<Int, Int>

    @BeforeEach
    fun initTree() {
        syncTree = SynchronizedBinarySearchTree<Int, Int>()
    }

    /**
     * It is possible that the root of the tree is removed with an unreleased lock
     * while another thread is waiting for a lock on the root of the tree.
     * The test shows that there will be no deadlock, because we require that any thread
     * attempting to lock the root must first lock a global tree lock.
     */
    @Test
    fun `Multiple threads root accessing`() {
        val keys = (0 until 100).toList()

        keys.parallelStream().forEach {
            syncTree[0] = 0
            syncTree.remove(0)
        }

        assertEquals(0, syncTree.size)
    }

    /**
     * The test shows that if multiple threads try to rewrite root's value
     * there will be no deadlock because this edge case is checked under
     * global tree lock.
     */
    @Test
    fun `No deadlock due to multiple rewriting of the root value`() {
        val values = (0 until 100).toList()

        values.parallelStream().forEach {
            syncTree[0] = it
        }

        assertEquals(1, syncTree.size)
    }

    /**
     * The test shows that if multiple threads try to remove node with same key
     * only one will be succeeded, because at the same time only one thread have
     * access to particular node.
     */
    @Test
    fun `Only one thread will delete node with same key`() {
        syncTree.run {
            this[1] = 1
            this[0] = 0
            this[2] = 2
        }

        (0 until 100).toList().parallelStream().forEach { syncTree.remove(0) }

        assertEquals(2, syncTree.size)
    }

    /**
     * Test shows that if we try to find a vertex that does not exist in the tree,
     * then the lock on the parent will be released.
     *
     * In this test tree is
     *
     * 0
     *  \
     *   1
     *    \
     *     2
     *      \
     *       3
     *
     * We will try to find node with key 4, it will not be succeeded. Parent will be 3.
     * Parent should be unlocked otherwise if we try to remove node with key 3 we will get deadlock.
     */
    @Test
    fun `After unsuccessful node search parent's lock is unlocked`() {
        repeat(4) { i -> syncTree[i] = i }

        syncTree[4]

        syncTree.remove(3)

        assertEquals(3, syncTree.size)
    }
}