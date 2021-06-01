package com.anleonov.indexer.api

/**
 * This interface allows one to index selected folder, cancel current indexing and
 * notify listeners about the indexing progress.
 */
interface DocumentIndexer {

    /**
     * Indexes folder [path].
     *
     * @param path a path to the folder
     */
    fun indexFolder(path: String)

    /**
     * Cancels current folder indexing process
     *
     */
    fun cancelIndexingFolder()

    /**
     * Add listener [listener] to get notifications from indexing process.
     *
     * @param listener (see [com.anleonov.indexer.api.DocumentIndexerListener])
     */
    fun addIndexerListener(listener: DocumentIndexerListener)

    /**
     * Remove listener [listener] in order to unsubscribe from notifications.
     *
     * @param listener (see [com.anleonov.indexer.api.DocumentIndexerListener])
     */
    fun removeIndexerListener(listener: DocumentIndexerListener)

}
