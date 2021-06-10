package com.anleonov.indexer.task

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentStore
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.AddLineIndexingEvent
import com.anleonov.indexer.model.IndexingEvent
import mu.KotlinLogging
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.BlockingQueue

private val logger = KotlinLogging.logger {}

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

    override fun run() {
        readDocument()
    }

    private fun readDocument() {
        val start = System.currentTimeMillis()
        val documentId = document.id
        val documentPath = document.path
        logger.debug { "Read file $documentPath for indexing" }

        fileSystemTracker.registerFile(documentPath)

        documentStore.addDocument(document)

        try {
            Files.lines(documentPath).use { lines ->
                lines.forEach { line: String ->
                    try {
                        indexingEventsQueue.put(AddLineIndexingEvent(documentId, line))
                    } catch (ex: InterruptedException) {
                        logger.warn(ex) { "Put line of file: $documentPath to queue interrupted" }
                    }
                }
            }
        } catch (ex: IOException) {
            logger.warn(ex) { "Reading file: $documentPath finished with exception" }
        }
        val end = System.currentTimeMillis()
        logger.debug { "File $documentPath reading and submitting took: ${end - start} ms" }
    }

}
