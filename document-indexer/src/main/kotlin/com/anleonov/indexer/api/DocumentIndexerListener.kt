package com.anleonov.indexer.api

/**
 * This interface allows one to get notifications from document indexing process.
 */
interface DocumentIndexerListener {

    /**
     * Tracks current indexing progress [progress]
     *
     * @param progress number which shows the current state of indexing
     */
    fun onIndexingInProgress(progress: Int)

    /**
     * Notifies when current indexing is finished
     */
    fun onIndexingFinished()

}
