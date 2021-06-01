package com.anleonov.searcher.util

/**
 * Finds all positions of [string] in string. If no positions found,
 * an empty list is returned.
 */
fun String.allIndicesOf(string: String): List<Int> {
    val result = mutableListOf<Int>()

    var index = 0
    while (index != -1) {
        index = this.indexOf(string, startIndex = index, ignoreCase = true)
        if (index != -1) {
            result.add(index)
            index = if (index + string.length < this.length) {
                index + string.length
            } else -1
        }
    }

    return result
}
