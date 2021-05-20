package com.anleonov.indexer.task

import com.anleonov.indexer.model.Document
import com.anleonov.indexer.model.IndexingEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue

class UpdateDocumentTask(
    private val document: Document,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>
) : Runnable {

    private val logger = LoggerFactory.getLogger(UpdateDocumentTask::class.java)

    override fun run() {
        val start = System.currentTimeMillis()

        val end = System.currentTimeMillis()
        logger.debug("Update index for file ${document.path} took ${end - start} ms")
    }

}
