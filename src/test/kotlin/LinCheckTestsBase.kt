import nodes.LockableNode
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.annotations.Validate
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.junit.jupiter.api.BeforeEach
import trees.SynchronizedBinarySearchTree

abstract class LinCheckTestsBase {

    var tree = SynchronizedBinarySearchTree<Int, Int>()

    @BeforeEach
    fun updateTree() {
        tree = SynchronizedBinarySearchTree()
    }

    @Operation
    fun set(
        @Param(gen = IntGen::class, conf = "-10:10") key: Int,
        @Param(gen = IntGen::class, conf = "-10:10") value: Int,
    ) {
        tree[key] = value
    }

    @Operation
    fun remove(
        @Param(gen = IntGen::class, conf = "-10:10") key: Int
    ) {
        tree.remove(key)
    }

    @Validate
    fun validateTree() {
        if (!tree.root.checkTreeInvariants()) {
            throw IllegalArgumentException("Invalid tree!")
        }
    }

    /**
     * Checks binary search tree invariants
     *
     * @return true if tree is valid else false
     *
     */
    internal fun LockableNode<Int, Int>?.checkTreeInvariants(
        mn: Int = Int.MIN_VALUE,
        mx: Int = Int.MAX_VALUE
    ): Boolean = when {
        this == null -> true
        value >= mx || value <= mn -> false
        else -> left.checkTreeInvariants(mn, value) && right.checkTreeInvariants(value, mx)
    }
}