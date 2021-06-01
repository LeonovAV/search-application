package com.anleonov.index

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentStore
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class DocumentStoreImpl : DocumentStore {

    // all documents, which are already indexed
    private val indexedDocuments = ConcurrentHashMap<Path, Document>()

    override fun containsDocument(documentPath: Path): Boolean {
        return indexedDocuments.containsKey(documentPath)
    }

    override fun getDocumentByPath(documentPath: Path): Document? {
        return indexedDocuments[documentPath]
    }

    override fun findDocumentsStartsWith(documentPath: Path): Collection<Document> {
        return indexedDocuments.filter { (path, _) -> path.startsWith(documentPath) }.values.toSet()
    }

    override fun findDocumentsByIds(documentIds: Set<Int>): Collection<Document> {
        return indexedDocuments.values.filter { it.id in documentIds }.toSet()
    }

    override fun findDocumentById(documentId: Int): Document? {
        return indexedDocuments.values.find { it.id == documentId }
    }

    override fun addDocument(document: Document) {
        indexedDocuments[document.path] = document
    }

    override fun removeDocument(documentPath: Path) {
        indexedDocuments.remove(documentPath)
    }

    override fun clear() {
        indexedDocuments.clear()
    }

}
