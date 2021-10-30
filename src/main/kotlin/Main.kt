import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

    val syncTree = SynchronizedBinarySearchTree<Int, Int>()
    val tree = BinarySearchTree<Int, Int>()



    val good = (0 until 100000).toList().shuffled()
    val bad = (50000 until  75000).shuffled()

    val t1 = measureTimeMillis {
        good.parallelStream().forEach { syncTree[it] = it; syncTree.remove(it) }
        bad.parallelStream().forEach { syncTree.remove(it) }
    }


    val t2 = measureTimeMillis {
        good.shuffled().forEach { tree[it] = it }
        bad.forEach { tree.remove(it) }
    }


      println("      par def")
      println("time: $t1 $t2")
      println("size: ${size(syncTree.root)} ${size(tree.root)}")
      println("vald: ${validateTree(syncTree.root)} ${validateTree(tree.root)}")
      println("vert ${allVertIn(syncTree, good, bad)}" +
              " ${allVertIn(tree, good, bad)}")
}

internal fun validateTree(
    root: LockableNode<Int, Int>?,
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

internal fun size(root: LockableNode<Int, Int>?): Int = when (root) {
    null -> 0
    else -> 1 + size(root.left) + size(root.right)
}

internal fun size(root: Node<Int, Int>?): Int = when (root) {
    null -> 0
    else -> 1 + size(root.left) + size(root.right)
}

internal fun allVertIn(tree: IBinarySearchTree<Int, Int>, good: List<Int>, bad: List<Int>): Boolean =
    (good - bad).all { tree[it] != null } && bad.all { tree[it] == null }