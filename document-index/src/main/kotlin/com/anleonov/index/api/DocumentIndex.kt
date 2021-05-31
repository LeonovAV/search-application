package com.anleonov.index.api

interface DocumentIndex {

    fun add(token: String, documentId: Int)

    fun remove(token: String, documentId: Int)

    fun update(token: String, documentId: Int)

    fun getDocumentIds(token: String): Set<Int>

    fun getDocumentIdsContains(tokenQuery: String): Set<Int>

    fun findTokensByDocumentId(documentId: Int): Set<String>

    fun addListener(listener: DocumentIndexTrackChangesListener)

    fun removeListener(listener: DocumentIndexTrackChangesListener)

    fun clear()

}
