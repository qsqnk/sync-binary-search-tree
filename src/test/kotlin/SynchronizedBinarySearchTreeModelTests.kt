import org.jetbrains.kotlinx.lincheck.LinChecker
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingCTest
import org.junit.jupiter.api.Test

@ModelCheckingCTest
class SynchronizedBinarySearchTreeModelTests : SynchronizedBinarySearchTreeTestTemplate() {

    @Test
    fun runTest() = LinChecker.check(this::class.java)

}