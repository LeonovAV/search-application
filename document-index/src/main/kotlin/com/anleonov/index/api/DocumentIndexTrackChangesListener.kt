package com.anleonov.index.api

/**
 *
 */
interface DocumentIndexTrackChangesListener {

    fun onTrackedTokenAdd(token: String, documentId: Int)

    fun onTrackedTokenUpdate(token: String, documentId: Int)

    fun onTrackedTokenRemove(token: String, documentId: Int)

}
