import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

fun trueMain() {
    val t = BinarySearchTree<Int, Int>()
    val good = (0 until 20000)
    val bad = (34 until 4444)

    good.forEach { t[it] = it }
    bad.forEach { t.remove(it) }
    println(
        (good - bad ).all { t[it] == it } && bad.all { t[it] == null }
    )
    println(good)
    println(bad)
}

fun main(args: Array<String>) {

    val syncTree = SynchronizedBinarySearchTree<Int, Int>()
    val tree = BinarySearchTree<Int, Int>()

    val nodes = 7

    val t1 = measureTimeMillis {
        (0 until nodes).toList().parallelStream().forEach { syncTree[it] = it }
        (2 until 5).toList().parallelStream().forEach { syncTree.remove(it) }
    }

    val t2 = measureTimeMillis {

        (0 until nodes).shuffled().forEach { tree[it] = it }
        (5 until 10).forEach { tree.remove(it) }

    }


      println("      par def")
      println("time: $t1 $t2")
      println("size: ${size(syncTree.root)} ${size(tree.root)}")
      println("vald: ${validateTree(syncTree.root)} ${validateTree(tree.root)}")
      println("vert ${allVertIn(syncTree, (0 until nodes).toList(), (2 until 5).toList())}" +
              " ${allVertIn(tree, (0 until nodes).toList(), (2 until 5).toList())}")
}

internal fun validateTree(
    root: SynchronizedNode<Int, Int>?,
    mn: Int = Int.MIN_VALUE,
    mx: Int = Int.MAX_VALUE
): Boolean = when {
    root == null -> true
    root.value >= mx || root.value <= mn -> false
    else -> validateTree(root.left, mn, root.value) && validateTree(root.right, root.value, mx)
}

internal fun validateTree(
    root: Node<Int, Int>?,
    mn: Int = Int.MIN_VALUE,
    mx: Int = Int.MAX_VALUE
): Boolean = when {
    root == null -> true
    root.value >= mx || root.value <= mn -> false
    else -> validateTree(root.left, mn, root.value) && validateTree(root.right, root.value, mx)
}

internal fun size(root: SynchronizedNode<Int, Int>?): Int = when (root) {
    null -> 0
    else -> 1 + size(root.left) + size(root.right)
}

internal fun size(root: Node<Int, Int>?): Int = when (root) {
    null -> 0
    else -> 1 + size(root.left) + size(root.right)
}

internal fun allVertIn(tree: IBinarySearchTree<Int, Int>, good: List<Int>, bad: List<Int>): Boolean =
    (good - bad).all { tree[it] != null } && bad.all { tree[it] == null }