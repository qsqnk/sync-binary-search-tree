import syncBST.LockableNode

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