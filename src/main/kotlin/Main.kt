fun main(args: Array<String>) {
    val tree = BinarySearchTree<Int, Int>()

    (1..10).forEach { tree[it] = it }
    (3..7).forEach(tree::remove)
}