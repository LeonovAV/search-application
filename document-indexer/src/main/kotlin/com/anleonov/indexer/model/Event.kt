package com.anleonov.indexer.model

sealed class IndexingEvent(
    val documentId: Int,
    val content: String
)

class AddLineIndexingEvent(
    documentId: Int,
    line: String
) : IndexingEvent(documentId, line)

class AddTokenIndexingEvent(
    documentId: Int,
    token: String
) : IndexingEvent(documentId, token)

class UpdateTokenIndexingEvent(
    documentId: Int,
    token: String
) : IndexingEvent(documentId, token)

class RemoveTokenIndexingEvent(
    documentId: Int,
    token: String
) : IndexingEvent(documentId, token)

enum class FileSystemEventType {
    CREATED, DELETED, MODIFIED
}
