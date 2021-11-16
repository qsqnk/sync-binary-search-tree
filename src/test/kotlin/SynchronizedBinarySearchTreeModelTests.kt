import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.annotations.Validate
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingCTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import trees.SynchronizedBinarySearchTree

@ModelCheckingCTest
class SynchronizedBinarySearchTreeModelTests {

    var tree = SynchronizedBinarySearchTree<Int, Int>()

    @BeforeEach
    fun updateTree() {
        tree = SynchronizedBinarySearchTree()
    }

    @Test
    fun runTest() = LinChecker.check(this::class.java)

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
        if (!tree.root.checkTreeInvariants()) throw IllegalArgumentException("Invalid tree!")
    }
}