package com.anleonov.searcher.api

sealed class SearchResult

data class AddSearchResult(
    val filePath: String,
    val lineNumber: Int,
    val positions: List<Int>
) : SearchResult()

data class RemoveSearchResult(
    val filePath: String,
    val lineNumber: Int
) : SearchResult()

data class UpdateSearchResult(
    val filePath: String,
    val lineNumber: Int,
    val positions: List<Int>
) : SearchResult()

/**
 * Special value indicates that current search process is complete
 */
class CompleteSearchResult : SearchResult()
