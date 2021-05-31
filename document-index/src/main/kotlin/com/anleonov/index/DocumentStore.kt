package com.anleonov.index

import com.anleonov.index.api.Document
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class DocumentStore {

    // all documents, which are already indexed
    private val indexedDocuments = ConcurrentHashMap<Path, Document>()

    fun containsDocument(documentPath: Path): Boolean {
        return indexedDocuments.containsKey(documentPath)
    }

    fun getDocumentByPath(documentPath: Path): Document? {
        return indexedDocuments[documentPath]
    }

    // TODO return set
    fun findDocumentsStartsWith(documentPath: Path): Collection<Document> {
        return indexedDocuments.filter { (path, _) -> path.startsWith(documentPath) }.values
    }

    // TODO return set
    fun findDocumentsByIds(documentIds: Set<Int>): Collection<Document> {
        return indexedDocuments.values.filter { it.id in documentIds }
    }

    fun findDocumentById(documentId: Int): Document? {
        return indexedDocuments.values.find { it.id == documentId }
    }

    fun addDocument(document: Document) {
        indexedDocuments[document.path] = document
    }

    fun removeDocument(documentPath: Path) {
        indexedDocuments.remove(documentPath)
    }

    fun clear() {
        indexedDocuments.clear()
    }

}
