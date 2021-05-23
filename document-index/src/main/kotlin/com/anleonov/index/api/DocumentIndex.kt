package com.anleonov.index.api

interface DocumentIndex {

    fun add(token: String, documentId: Int)

    fun remove(token: String, documentId: Int)

    fun update(token: String, documentId: Int)

    fun findTokensByDocumentId(documentId: Int): Set<String>

}
