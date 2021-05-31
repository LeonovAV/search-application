package com.anleonov.indexer.task

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.Tokenizer
import com.anleonov.indexer.model.AddTokenIndexingEvent
import com.anleonov.indexer.model.IndexingEvent
import com.anleonov.indexer.model.RemoveTokenIndexingEvent
import com.anleonov.indexer.model.UpdateTokenIndexingEvent
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.concurrent.BlockingQueue

class UpdateDocumentTask(
    private val document: Document,
    private val tokenizer: Tokenizer,
    private val documentIndex: DocumentIndex,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>
) : Runnable {

    private val logger = LoggerFactory.getLogger(UpdateDocumentTask::class.java)

    override fun run() {
        val start = System.currentTimeMillis()

        val documentId = document.id

        val documentTokens = documentIndex.findTokensByDocumentId(documentId).toMutableSet()
        val documentTokensToUpdate = mutableSetOf<String>()

        Files.lines(document.path).use { lines ->
            lines.forEach { line ->
                tokenizer.tokenize(line).forEach { token ->
                    val tokenContent = token.content
                    if (tokenContent in documentTokens) {
                        documentTokens.remove(tokenContent)
                        documentTokensToUpdate.add(tokenContent)
                    } else {
                        try {
                            indexingEventsQueue.put(AddTokenIndexingEvent(documentId, tokenContent))
                        } catch (ex: InterruptedException) {
                            logger.warn("Put add token event to queue finished with exception", ex)
                        }
                    }

                    documentTokensToUpdate.forEach {
                        try {
                            indexingEventsQueue.put(UpdateTokenIndexingEvent(documentId, it))
                        } catch (ex: InterruptedException) {
                            logger.warn("Put update token event to queue finished with exception", ex)
                        }
                    }

                    documentTokens.forEach {
                        try {
                            indexingEventsQueue.put(RemoveTokenIndexingEvent(documentId, it))
                        } catch (ex: InterruptedException) {
                            logger.warn("Put delete token event to queue finished with exception", ex)
                        }
                    }
                }
            }
        }

        val end = System.currentTimeMillis()
        logger.debug("Update index for file ${document.path} took ${end - start} ms")
    }

}
