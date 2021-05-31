package com.anleonov.indexer.task

import com.anleonov.index.DocumentStore
import com.anleonov.index.api.Document
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.IndexingEvent
import java.util.concurrent.BlockingQueue

/**
 *
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
