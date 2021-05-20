package com.anleonov.indexer.task

import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.Document
import com.anleonov.indexer.model.IndexingEvent
import java.nio.file.Path
import java.util.concurrent.BlockingQueue

/**
 *
 */
class ReadDocumentTask(
    document: Document,
    indexedDocuments: MutableMap<Path, Document>,
    fileSystemTracker: FileSystemTracker,
    indexingEventsQueue: BlockingQueue<IndexingEvent>
) : AbstractReadDocumentTask(document, indexedDocuments, fileSystemTracker, indexingEventsQueue), Runnable {

    override fun run() {
        super.readDocument()
    }

}
