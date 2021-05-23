package com.anleonov.indexer.task

import com.anleonov.index.api.DocumentIndex
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.Document
import com.anleonov.indexer.model.IndexingEvent
import com.anleonov.indexer.model.RemoveTokenIndexingEvent
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.BlockingQueue

class RemoveDocumentTask(
    private val document: Document,
    private val documentIndex: DocumentIndex,
    private val indexedDocuments: MutableMap<Path, Document>,
    private val fileSystemTracker: FileSystemTracker,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>
) : Runnable {

    private val logger = LoggerFactory.getLogger(RemoveDocumentTask::class.java)

    override fun run() {
        val start = System.currentTimeMillis()
        val documentId = document.id
        val documentPath = document.path

        val documentTokensToRemove = documentIndex.findTokensByDocumentId(documentId)
        documentTokensToRemove.forEach {
            try {
                indexingEventsQueue.put(RemoveTokenIndexingEvent(documentId, it))
            } catch (ex: InterruptedException) {
                logger.warn("Error during put event to indexing queue", ex)
            }
        }

        indexedDocuments.remove(documentPath)

        fileSystemTracker.unregisterFile(documentPath)

        val end = System.currentTimeMillis()
        logger.debug("Remove file ${document.path} from index took ${end - start} ms")
    }

}
