import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.jupiter.api.Test

@StressCTest.StressCTests
class SynchronizedBinarySearchTreeStressTests : SynchronizedBinarySearchTreeTestTemplate() {

    @Test
    fun runTest() = StressOptions()
        .sequentialSpecification(SequentialImplementation::class.java)
        .check(this::class)

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