package com.anleonov.indexer.api

/**
 *
 */
interface DocumentIndexerListener {

    fun onIndexingInProgress(progress: Int)

    fun onIndexingFinished()

}
