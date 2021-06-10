package com.anleonov.indexer.task

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.DocumentStore
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.IndexingEvent
import com.anleonov.indexer.model.RemoveTokenIndexingEvent
import mu.KotlinLogging
import java.util.concurrent.BlockingQueue

private val logger = KotlinLogging.logger {}

class RemoveDocumentTask(
    private val document: Document,
    private val documentIndex: DocumentIndex,
    private val documentStore: DocumentStore,
    private val fileSystemTracker: FileSystemTracker,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>
) : Runnable {

    override fun run() {
        val start = System.currentTimeMillis()
        val documentId = document.id
        val documentPath = document.path

        val documentTokensToRemove = documentIndex.findTokensByDocumentId(documentId)
        documentTokensToRemove.forEach {
            try {
                indexingEventsQueue.put(RemoveTokenIndexingEvent(documentId, it))
            } catch (ex: InterruptedException) {
                logger.warn(ex) { "Error during put event to indexing queue" }
            }
        }

        documentStore.removeDocument(documentPath)

        fileSystemTracker.unregisterFile(documentPath)

        val end = System.currentTimeMillis()
        logger.debug { "Remove file ${document.path} from index took ${end - start} ms" }
    }

}
