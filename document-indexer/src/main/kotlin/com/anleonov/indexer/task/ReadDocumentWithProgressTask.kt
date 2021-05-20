package com.anleonov.indexer.task

import com.anleonov.indexer.api.DocumentIndexerListener
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.Document
import com.anleonov.indexer.model.IndexingEvent
import java.nio.file.Path
import java.util.concurrent.BlockingQueue

/**
 *
 */
class ReadDocumentWithProgressTask(
    document: Document,
    private val documentNumber: Int,
    private val percentage: Double,
    indexedDocuments: MutableMap<Path, Document>,
    fileSystemTracker: FileSystemTracker,
    indexingEventsQueue: BlockingQueue<IndexingEvent>,
    private val listeners: List<DocumentIndexerListener>
) : AbstractReadDocumentTask(document, indexedDocuments, fileSystemTracker, indexingEventsQueue), Runnable {

    override fun run() {
        super.readDocument()

        listeners.forEach { it.onIndexingInProgress((documentNumber / percentage).toInt()) }
    }

}
