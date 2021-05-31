package com.anleonov.searcher.api

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 *
 */
interface DocumentSearcher {

    fun search(query: String): MutableSharedFlow<SearchResult>

}
