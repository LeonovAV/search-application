package com.anleonov.index.api

import java.nio.file.Path

interface DocumentStore {

    fun containsDocument(documentPath: Path): Boolean

    fun getDocumentByPath(documentPath: Path): Document?

    fun findDocumentsStartsWith(documentPath: Path): Collection<Document>

    fun findDocumentsByIds(documentIds: Set<Int>): Collection<Document>

    fun findDocumentById(documentId: Int): Document?

    fun addDocument(document: Document)

    fun removeDocument(documentPath: Path)

    fun clear()

}
