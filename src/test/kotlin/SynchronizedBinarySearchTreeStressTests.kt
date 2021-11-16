import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.annotations.Validate
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import trees.SynchronizedBinarySearchTree

@StressCTest.StressCTests
class SynchronizedBinarySearchTreeStressTests  {

    var tree = SynchronizedBinarySearchTree<Int, Int>()

    @BeforeEach
    fun updateTree() { tree = SynchronizedBinarySearchTree() }

    @Test
    fun runTest() = StressOptions()
        .sequentialSpecification(SequentialImplementation::class.java)
        .check(this::class)

    @Operation
    fun set(
        @Param(gen = IntGen::class, conf = "-10:10") key: Int,
        @Param(gen = IntGen::class, conf = "-10:10") value: Int,
    ) { tree[key] = value }

    @Operation
    fun remove(
        @Param(gen = IntGen::class, conf = "-10:10") key: Int
    ) { tree.remove(key) }

    @Validate
    fun validateTree() {
        if (!tree.root.checkTreeInvariants()) throw IllegalArgumentException("Invalid tree!")
    }

    class SequentialImplementation : VerifierState() {
        val sequentialImplementation = hashMapOf<Int, Int>()

        fun set(key: Int, value: Int) {
            sequentialImplementation[key] = value
        }

        fun remove(key: Int) {
            sequentialImplementation.remove(key)
        }

        override fun extractState() = sequentialImplementation
    }
}