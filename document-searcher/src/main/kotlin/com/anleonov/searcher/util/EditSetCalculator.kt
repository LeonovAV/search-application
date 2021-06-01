package com.anleonov.searcher.util

/**
 * Calculates [EditSet] between collection B (before) and collection A (after).
 *
 * @param B collection B type
 * @param A collection A type
 * @param K generic identity for B and A types
 *
 */
class EditSetCalculator<K: Any, B: Any, A: Any>(
    val before: Collection<B>,
    val after: Collection<A>,
    val beforeKey: (B) -> K,
    val afterKey: (A) -> K
) {

    fun calculate(): EditSet<B, A> {
        val map = mutableMapOf<K, Pair<B?, A?>>()
        before.forEach { map[beforeKey(it)] = it to null }
        after.forEach {
            val aKey = afterKey(it)
            val bValue = map[aKey]
            if (bValue == null) {
                map[aKey] = null to it
            } else {
                map[aKey] = bValue.first to it
            }
        }
        return EditSet(
            operations = map.mapNotNull { (_, v) ->
                val (old, new) = v
                val operation: EditOperation<B, A>? = when {
                    old == null && new != null -> InsertOperation(new)
                    old != null && new == null -> DeleteOperation(old)
                    old != null && new != null -> KeepOperation(old, new)
                    else -> null
                }
                operation
            }
        )
    }

}

/**
 * Operation set that made collection A from collection B.
 *
 * [InsertOperation] object was not present in B but appeared in A
 *
 * [KeepOperation] object was present in B but was kept in A (but might be changed)
 *
 * [DeleteOperation] object was present in B but disappeared in A
 *
 * @see [EditOperation]
 */
data class EditSet<B: Any, A: Any>(
    val operations: List<EditOperation<B, A>>
)

sealed class EditOperation<B: Any, A: Any>

data class InsertOperation<B: Any, A: Any>(
    val newObject: A
): EditOperation<B, A>()

data class KeepOperation<B: Any, A: Any>(
    val oldObject: B,
    val newObject: A
): EditOperation<B, A>()

data class DeleteOperation<B: Any, A: Any>(
    val oldObject: B
): EditOperation<B, A>()
