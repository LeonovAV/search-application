package com.anleonov.indexer.api

/**
 *
 */
interface DocumentIndexer {

    fun indexFolder(path: String)

    fun cancelIndexingFolder()

    fun addIndexerListener(listener: DocumentIndexerListener)

    fun removeIndexerListener(listener: DocumentIndexerListener)

}
