package com.anleonov.searcher

import com.anleonov.index.api.*
import com.anleonov.index.api.CommonNGramSize.triGram
import com.anleonov.searcher.api.*
import com.anleonov.searcher.util.*
import kotlinx.coroutines.flow.MutableSharedFlow
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Class is responsible for performing full-text search in document index and returns search results
 * as a flow in order to keep them up to date.
 */
class DocumentSearcherManager(
    private val tokenizer: Tokenizer,
    private val documentIndex: DocumentIndex,
    private val documentStore: DocumentStore
) : DocumentSearcher, DocumentIndexTrackChangesListener {

    private val numberOfResults = 100

    private lateinit var currentSharedFlow: MutableSharedFlow<SearchResult>

    // Track current state of search
    private var currentSearchQuery: String = ""
    private var trackedTokens: Set<String> = emptySet()
    private var matchedDocuments: MutableList<MatchedDocument> = mutableListOf()

    init {
        // Document searcher is a listener for index store in order to reflect changes, which are relevant to
        // the current search query from UI
        documentIndex.addListener(this)
    }

    override fun search(query: String): MutableSharedFlow<SearchResult> {
        logger.debug { "Search query $query" }

        if (this::currentSharedFlow.isInitialized) {
            currentSharedFlow.tryEmit(CompleteSearchResult())
        }

        currentSharedFlow = MutableSharedFlow(numberOfResults)

        if (query.isEmpty()) {
            currentSharedFlow.tryEmit(CompleteSearchResult())
            return currentSharedFlow
        }

        val queryLc = query.toLowerCase()

        currentSearchQuery = queryLc

        val documentIdsCandidates = when {
            queryLc.length < triGram -> {
                trackedTokens = setOf(queryLc)
                documentIndex.getDocumentIdsContains(queryLc)
            }
            queryLc.length == triGram -> {
                trackedTokens = setOf(queryLc)
                documentIndex.getDocumentIds(queryLc)
            }
            else -> {
                // Query has several tokens, document should contain all of these to be a candidate
                val tokens = tokenizer.tokenize(queryLc)
                trackedTokens = tokens.mapTo(HashSet()) { it.content }
                val documentCandidatesForFirstToken = documentIndex.getDocumentIds(tokens[0].content).toMutableSet()
                if (documentCandidatesForFirstToken.isEmpty()) {
                    currentSharedFlow.tryEmit(CompleteSearchResult())
                    return currentSharedFlow
                }

                for (i in 1 until tokens.size) {
                    val documentIdsForToken = documentIndex.getDocumentIds(tokens[i].content)
                    documentCandidatesForFirstToken.removeIf { it !in documentIdsForToken }

                    if (documentCandidatesForFirstToken.isEmpty()) {
                        currentSharedFlow.tryEmit(CompleteSearchResult())
                        return currentSharedFlow
                    }
                }

                documentCandidatesForFirstToken
            }
        }
        if (documentIdsCandidates.isEmpty()) {
            currentSharedFlow.tryEmit(CompleteSearchResult())
            return currentSharedFlow
        }

        val documentCandidates = documentStore.findDocumentsByIds(documentIdsCandidates)

        logger.debug { "Document candidates are: ${documentCandidates.map { it.path }}" }

        matchedDocuments = documentCandidates
            .asSequence()
            .filter { Files.exists(it.path) }
            .mapNotNull { it.toMatchedDocument(queryLc) }
            .take(numberOfResults)
            .toMutableList()

        matchedDocuments.forEach {
            logger.debug { "Found matches: $it" }
            it.toAddSearchResult().forEach { currentSharedFlow.tryEmit(it) }
        }

        return currentSharedFlow
    }

    override fun onTrackedTokenAdd(token: String, documentId: Int) {
        if (trackedTokens.contains(token) && matchedDocuments.size < numberOfResults) {
            val matchedDocument = matchedDocuments.find { it.documentId == documentId }
            if (matchedDocument == null) {
                val newMatchedDocument = documentStore.findDocumentById(documentId)?.toMatchedDocument(currentSearchQuery)
                if (newMatchedDocument != null) {
                    matchedDocuments.add(newMatchedDocument)
                    newMatchedDocument.toAddSearchResult().forEach { currentSharedFlow.tryEmit(it) }
                }
            } else {
                processTrackedTokenUpdate(matchedDocument)
            }
        }
    }

    override fun onTrackedTokenUpdate(token: String, documentId: Int) {
        if (trackedTokens.contains(token)) {
            val matchedDocument = matchedDocuments.find { it.documentId == documentId }
            if (matchedDocument != null) {
                processTrackedTokenUpdate(matchedDocument)
            }
        }
    }

    override fun onTrackedTokenRemove(token: String, documentId: Int) {
        if (trackedTokens.contains(token)) {
            val matchedDocument = matchedDocuments.find { it.documentId == documentId }
            if (matchedDocument != null) {
                processTrackedTokenUpdate(matchedDocument)
            }
        }
    }

    private fun Document.toMatchedDocument(queryLc: String): MatchedDocument? {
        var linesCounter = 0
        val matchedLines = mutableMapOf<Int, List<Int>>()
        // File could be deleted at all - do not read it from file system
        if (Files.exists(this.path)) {
            Files.lines(this.path).use { lines ->
                lines.forEach { line ->
                    val matchedPositions = line.allIndicesOf(queryLc)

                    if (matchedPositions.isNotEmpty()) {
                        matchedLines[linesCounter] = matchedPositions
                    }
                    linesCounter++
                }
            }
        }

        return if (matchedLines.isNotEmpty()) {
            MatchedDocument(
                documentId = this.id,
                path = this.path,
                matchedLines = matchedLines
            )
        } else null
    }

    private fun MatchedDocument.toAddSearchResult(): List<SearchResult> {
        return this.matchedLines.map { (lineNumber, positions) ->
            AddSearchResult(
                filePath = this.path.toAbsolutePath().toString(),
                lineNumber = lineNumber,
                positions = positions
            )
        }
    }

    private fun processTrackedTokenUpdate(matchedDocument: MatchedDocument) {
        val oldMatchedLines = matchedDocument.matchedLines
        val filePath = matchedDocument.path
        val filePathAsString = matchedDocument.path.toAbsolutePath().toString()
        val newMatchedDocument = Document(
            id = matchedDocument.documentId,
            path = filePath,
            parentPath = matchedDocument.path.parent
        ).toMatchedDocument(currentSearchQuery)

        if (newMatchedDocument != null) {
            val newMatchedLines = newMatchedDocument.matchedLines
            val editSet = EditSetCalculator(
                before = oldMatchedLines.keys,
                after = newMatchedLines.keys,
                beforeKey = { it },
                afterKey = { it }
            ).calculate()

            editSet.operations.forEach { operation ->
                when (operation) {
                    is InsertOperation -> {
                        currentSharedFlow.tryEmit(
                            AddSearchResult(
                                filePath = filePathAsString,
                                lineNumber = operation.newObject,
                                positions = newMatchedLines.getValue(operation.newObject)
                            )
                        )
                    }
                    is KeepOperation -> {
                        // Update existing one with new positions
                        currentSharedFlow.tryEmit(
                            UpdateSearchResult(
                                filePath = filePathAsString,
                                lineNumber = operation.newObject,
                                positions = newMatchedLines.getValue(operation.newObject)
                            )
                        )
                    }
                    is DeleteOperation -> {
                        currentSharedFlow.tryEmit(
                            RemoveSearchResult(
                                filePath = filePathAsString,
                                lineNumber = operation.oldObject
                            )
                        )
                    }
                }
            }

            // Remove old matched document and insert the new one
            matchedDocuments.remove(matchedDocument)
            matchedDocuments.add(newMatchedDocument)
        } else {
            // After changes document is not relevant for search query
            matchedDocuments.remove(matchedDocument)

            matchedDocument.matchedLines.forEach { (lineNumber, _) ->
                currentSharedFlow.tryEmit(
                    RemoveSearchResult(
                        filePath = filePathAsString,
                        lineNumber = lineNumber
                    )
                )
            }
        }
    }

    private data class MatchedDocument(
        val documentId: Int,
        val path: Path,
        val matchedLines: Map<Int, List<Int>>
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MatchedDocument) return false

            if (documentId != other.documentId) return false

            return true
        }

        override fun hashCode(): Int {
            return documentId
        }
    }

}
