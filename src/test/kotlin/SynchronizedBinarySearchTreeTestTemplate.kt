import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.annotations.Validate
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import syncBST.SynchronizedBinarySearchTree

abstract class SynchronizedBinarySearchTreeTestTemplate {

    private var tree = SynchronizedBinarySearchTree<Int, Int>()

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
    ) = tree.remove(key)

    @Operation
    fun get(
        @Param(gen = IntGen::class, conf = "-10:10") key: Int
    ) = tree[key]


    @Validate
    fun validateTree() = check(tree.root.checkTreeInvariants()) { "Tree invariants violated" }
}