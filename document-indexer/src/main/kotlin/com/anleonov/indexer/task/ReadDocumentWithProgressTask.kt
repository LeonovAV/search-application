package com.anleonov.indexer.task

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentStore
import com.anleonov.indexer.api.DocumentIndexerListener
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.IndexingEvent
import java.util.concurrent.BlockingQueue

/**
 * Class allows one to notify listeners about current progress
 * (see [com.anleonov.indexer.task.AbstractReadDocumentTask])
 */
class ReadDocumentWithProgressTask(
    document: Document,
    private val documentNumber: Int,
    private val percentage: Double,
    documentStore: DocumentStore,
    fileSystemTracker: FileSystemTracker,
    indexingEventsQueue: BlockingQueue<IndexingEvent>,
    private val listeners: List<DocumentIndexerListener>
) : AbstractReadDocumentTask(document, documentStore, fileSystemTracker, indexingEventsQueue), Runnable {

    override fun run() {
        super.readDocument()

        listeners.forEach { it.onIndexingInProgress((documentNumber / percentage).toInt()) }
    }

}
