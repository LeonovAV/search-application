package com.anleonov.indexer.task

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.Tokenizer
import com.anleonov.indexer.model.AddTokenIndexingEvent
import com.anleonov.indexer.model.IndexingEvent
import com.anleonov.indexer.model.RemoveTokenIndexingEvent
import com.anleonov.indexer.model.UpdateTokenIndexingEvent
import mu.KotlinLogging
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.BlockingQueue

private val logger = KotlinLogging.logger {}

class UpdateDocumentTask(
    private val document: Document,
    private val tokenizer: Tokenizer,
    private val documentIndex: DocumentIndex,
    private val indexingEventsQueue: BlockingQueue<IndexingEvent>
) : Runnable {

    override fun run() {
        val start = System.currentTimeMillis()

        val documentId = document.id

        val documentTokens = documentIndex.findTokensByDocumentId(documentId).toMutableSet()
        val documentTokensToUpdate = mutableSetOf<String>()

        try {
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
                                logger.warn(ex) { "Put add token event to queue finished with exception" }
                            }
                        }

                        documentTokensToUpdate.forEach {
                            try {
                                indexingEventsQueue.put(UpdateTokenIndexingEvent(documentId, it))
                            } catch (ex: InterruptedException) {
                                logger.warn(ex) { "Put update token event to queue finished with exception" }
                            }
                        }

                        documentTokens.forEach {
                            try {
                                indexingEventsQueue.put(RemoveTokenIndexingEvent(documentId, it))
                            } catch (ex: InterruptedException) {
                                logger.warn(ex) { "Put delete token event to queue finished with exception" }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            logger.warn(e) { "Update index for file ${document.path} finished with exception" }
        }

        val end = System.currentTimeMillis()
        logger.debug { "Update index for file ${document.path} took ${end - start} ms" }
    }

}
