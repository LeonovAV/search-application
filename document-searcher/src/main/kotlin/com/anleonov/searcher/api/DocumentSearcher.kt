package com.anleonov.searcher.api

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * This interface allows one to search text data based on simple query.
 */
interface DocumentSearcher {

    /**
     * Searches [query] in document index.
     *
     * @param query search query
     */
    fun search(query: String): MutableSharedFlow<SearchResult>

}
