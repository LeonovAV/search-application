package com.anleonov.searcher.api

/**
 *
 */
interface DocumentSearcher {

    fun search(query: String): List<Int>

}
