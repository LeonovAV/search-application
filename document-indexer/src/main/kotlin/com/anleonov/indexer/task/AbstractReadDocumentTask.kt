package com.anleonov.indexer.task

import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.AddLineIndexingEvent
import com.anleonov.indexer.model.Document
import com.anleonov.indexer.model.IndexingEvent
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.BlockingQueue

/**
 *
 */
abstract class AbstractReadDocumentTask(
    private val document: Document,
    private val indexedDocuments: MutableMap<Path, Document>,
    private val fileSystemTracker: FileSystemTracker,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>
) {

    private val logger = LoggerFactory.getLogger(AbstractReadDocumentTask::class.java)

    fun readDocument() {
        val start = System.currentTimeMillis()
        val documentId = document.id
        val documentPath = document.path
        logger.debug("Read file $documentPath for indexing")
        try {
            Files.lines(documentPath).use { lines ->
                lines.forEach { line: String ->
                    try {
                        indexingEventsQueue.put(AddLineIndexingEvent(documentId, line))
                    } catch (ex: InterruptedException) {
                        logger.warn("Put line of file: $documentPath to queue interrupted", ex)
                    }
                }
            }
        } catch (ex: IOException) {
            logger.warn("Reading file: $documentPath finished with exception", ex)
        }
        val end = System.currentTimeMillis()
        logger.debug("File $documentPath reading and submitting took: ${end - start} ms")

        fileSystemTracker.registerFile(documentPath)

        indexedDocuments[documentPath] = document
    }

}
