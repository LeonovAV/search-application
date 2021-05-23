package com.anleonov.indexer.task

import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.Tokenizer
import com.anleonov.indexer.api.DocumentIndexerListener
import com.anleonov.indexer.model.*
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue

class DocumentIndexingJob(
    private val tokenizer: Tokenizer,
    private val documentIndex: DocumentIndex,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>,
    private val listeners: List<DocumentIndexerListener>
) : Runnable {

    private val logger = LoggerFactory.getLogger(DocumentIndexingJob::class.java)

    private val numberOfElementsFromQueue = 10_000

    override fun run() {
        val indexingEvents = mutableListOf<IndexingEvent>()
        val transferredElements = indexingEventsQueue.drainTo(indexingEvents, numberOfElementsFromQueue)
        logger.trace("Number of transferred elements - $transferredElements")

        if (indexingEvents.isEmpty()) {
            logger.trace("Indexing finished")
            listeners.forEach { it.onIndexingFinished() }
        }

        indexingEvents.forEach {
            logger.debug("$it")
            val affectedDocumentId = it.documentId
            when (it) {
                is AddLineIndexingEvent -> {
                    tokenizer.tokenize(it.content).forEach { token ->
                        documentIndex.add(token.content, affectedDocumentId)
                    }
                }
                is RemoveTokenIndexingEvent -> {
                    documentIndex.remove(it.content, affectedDocumentId)
                }
                is AddTokenIndexingEvent -> {
                    documentIndex.add(it.content, affectedDocumentId)
                }
                is UpdateTokenIndexingEvent -> {
                    documentIndex.update(it.content, affectedDocumentId)
                }
            }
        }
    }

}
