package com.anleonov.index

import com.anleonov.index.api.DocumentIndex

class DocumentIndexStore : DocumentIndex {

    override fun add(token: String, documentId: Int) {
    }

    override fun remove(token: String, documentId: Int) {
    }

    override fun update(token: String, documentId: Int) {
    }

    override fun findTokensByDocumentId(documentId: Int): Set<String> {
        return emptySet()
    }

}
