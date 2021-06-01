package com.anleonov.indexer.task

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentStore
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.IndexingEvent
import java.util.concurrent.BlockingQueue

/**
 * See [com.anleonov.indexer.task.AbstractReadDocumentTask].
 */
class ReadDocumentTask(
    document: Document,
    documentStore: DocumentStore,
    fileSystemTracker: FileSystemTracker,
    indexingEventsQueue: BlockingQueue<IndexingEvent>
) : AbstractReadDocumentTask(document, documentStore, fileSystemTracker, indexingEventsQueue), Runnable {

    override fun run() {
        super.readDocument()
    }

}
