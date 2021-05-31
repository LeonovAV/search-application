package com.anleonov.searcher.api

sealed class SearchResult(
    val filePath: String,
    val lineNumber: Int,
    val positions: List<Int>
) {

    override fun toString(): String {
        return "SearchResult(filePath='$filePath', lineNumber=$lineNumber, positions=$positions)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SearchResult) return false

        if (filePath != other.filePath) return false
        if (lineNumber != other.lineNumber) return false
        if (positions != other.positions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + lineNumber
        result = 31 * result + positions.hashCode()
        return result
    }

}

class AddSearchResult(
    filePath: String,
    lineNumber: Int,
    positions: List<Int>
) : SearchResult(filePath, lineNumber, positions)

class RemoveSearchResult(
    filePath: String,
    lineNumber: Int,
    positions: List<Int>
) : SearchResult(filePath, lineNumber, positions)

class UpdateSearchResult(
    filePath: String,
    lineNumber: Int,
    positions: List<Int>
) : SearchResult(filePath, lineNumber, positions)

/**
 * Special value indicates that current search process is complete
 */
class CompleteSearchResult : SearchResult("", -1, emptyList())
