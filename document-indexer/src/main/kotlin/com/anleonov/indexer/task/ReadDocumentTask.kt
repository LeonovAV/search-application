package com.anleonov.indexer.task

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentStore
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.AddLineIndexingEvent
import com.anleonov.indexer.model.IndexingEvent
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.BlockingQueue

/**
 * Class is responsible for document indexing and registering file in the filesystem
 * tracker. Task is used for concurrent indexing. It reads the file and post an event
 * with line content to the indexing queue.
 */
open class ReadDocumentTask(
    private val document: Document,
    private val documentStore: DocumentStore,
    private val fileSystemTracker: FileSystemTracker,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>
) : Runnable {

    private val logger = LoggerFactory.getLogger(ReadDocumentTask::class.java)

    override fun run() {
        readDocument()
    }

    private fun readDocument() {
        val start = System.currentTimeMillis()
        val documentId = document.id
        val documentPath = document.path
        logger.debug("Read file $documentPath for indexing")

        fileSystemTracker.registerFile(documentPath)

        documentStore.addDocument(document)

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
    }

}
