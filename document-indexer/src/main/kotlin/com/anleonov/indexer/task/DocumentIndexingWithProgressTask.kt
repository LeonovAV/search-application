package com.anleonov.indexer.task

import com.anleonov.indexer.api.DocumentIndexerListener
import com.anleonov.indexer.model.IndexingEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue

class DocumentIndexingWithProgressTask(
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>,
    private val listeners: List<DocumentIndexerListener>
) : Runnable {

    private val logger = LoggerFactory.getLogger(DocumentIndexingWithProgressTask::class.java)

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
        }
    }

}
