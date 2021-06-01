package com.anleonov.searcher

import com.anleonov.index.DocumentIndexStore
import com.anleonov.index.DocumentStoreImpl
import com.anleonov.index.api.CommonNGramSize.triGram
import com.anleonov.index.api.Document
import com.anleonov.index.tokenizer.NGramTokenizer
import com.anleonov.searcher.api.AddSearchResult
import com.anleonov.searcher.api.CompleteSearchResult
import com.anleonov.searcher.api.SearchResult
import com.anleonov.searcher.util.allIndicesOf
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class DocumentSearcherManagerTest {

    private val tokenizer = NGramTokenizer(triGram)
    private val documentIndex = DocumentIndexStore()
    private val documentStore = DocumentStoreImpl()

    private val documentSearcherManager = DocumentSearcherManager(tokenizer, documentIndex, documentStore)

    private val documentFilePaths = mutableListOf<Path>()

    @Before
    fun init() {
        var documentId = 0
        Files.walkFileTree(Paths.get("src", "test", "resources", "search", "small"), object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.lines(file).use { lines ->
                    lines.forEach { line ->
                        tokenizer.tokenize(line).forEach { token ->
                            documentIndex.add(token.content, documentId)
                        }
                    }
                }
                documentStore.addDocument(Document(documentId, file, file.parent))

                // For test purposes
                documentFilePaths.add(file)

                documentId++
                return FileVisitResult.CONTINUE
            }
        })

        documentFilePaths.sort()
    }

    @Test
    fun `should return empty result for empty string`() {
        val searchResult = documentSearcherManager.search("")

        // Complete flow
        searchResult.tryEmit(CompleteSearchResult())

        runBlocking {
            assertNull(searchResult.takeWhile { it !is CompleteSearchResult }.firstOrNull())
        }
    }

    @Test
    fun `should return empty result for unknown string`() {
        val searchResult = documentSearcherManager.search("abc")

        // Complete flow
        searchResult.tryEmit(CompleteSearchResult())

        runBlocking {
            assertNull(searchResult.takeWhile { it !is CompleteSearchResult }.firstOrNull())
        }
    }

    @Test
    fun `should find one letter result in several documents with several positions in lines`() {
        val searchQuery = "m"
        val searchResult = documentSearcherManager.search(searchQuery)

        // Complete flow
        searchResult.tryEmit(CompleteSearchResult())

        val expectedResult = generateExpectedResults(searchQuery, documentFilePaths)

        val result = mutableListOf<SearchResult>()
        runBlocking {
            searchResult
                .takeWhile { it !is CompleteSearchResult }
                .onEach { result.add(it) }
                .collect()
        }
        assertEquals(expectedResult.sortedBy { it.filePath }, result.sortedBy { it.filePath })
    }

    @Test
    fun `should find two letters result in several documents with several positions in lines`() {
        val searchQuery = "bi"
        val searchResult = documentSearcherManager.search(searchQuery)

        // Complete flow
        searchResult.tryEmit(CompleteSearchResult())

        // Document TestB.txt does not contain search query
        val documentContainsSearchQuery = documentFilePaths.toMutableList()
        documentContainsSearchQuery.remove(documentFilePaths[1])
        val expectedResult = generateExpectedResults(searchQuery, documentContainsSearchQuery)

        val result = mutableListOf<SearchResult>()
        runBlocking {
            searchResult
                .takeWhile { it !is CompleteSearchResult }
                .onEach { result.add(it) }
                .collect()
        }
        assertEquals(expectedResult.sortedBy { it.filePath }, result.sortedBy { it.filePath })
    }

    @Test
    fun `should find three letters result in several documents with several positions in lines`() {
        val searchQuery = "bor"
        val searchResult = documentSearcherManager.search(searchQuery)

        // Complete flow
        searchResult.tryEmit(CompleteSearchResult())

        // Only TestA.txt contains search query
        val documentContainsSearchQuery = listOf(documentFilePaths[0])
        val expectedResult = generateExpectedResults(searchQuery, documentContainsSearchQuery)

        val result = mutableListOf<SearchResult>()
        runBlocking {
            searchResult
                .takeWhile { it !is CompleteSearchResult }
                .onEach { result.add(it) }
                .collect()
        }
        assertEquals(expectedResult.sortedBy { it.filePath }, result.sortedBy { it.filePath })
    }

    @Test
    fun `should find long string result in several documents`() {
        val searchQuery = "grandmother"
        val searchResult = documentSearcherManager.search(searchQuery)

        // Complete flow
        searchResult.tryEmit(CompleteSearchResult())

        // Document TestC.txt does not contain search query
        val documentContainsSearchQuery = documentFilePaths.toMutableList()
        documentContainsSearchQuery.remove(documentFilePaths[2])
        val expectedResult = generateExpectedResults(searchQuery, documentContainsSearchQuery)

        val result = mutableListOf<SearchResult>()
        runBlocking {
            searchResult
                .takeWhile { it !is CompleteSearchResult }
                .onEach { result.add(it) }
                .collect()
        }
        assertEquals(expectedResult.sortedBy { it.filePath }, result.sortedBy { it.filePath })
    }

    @Test
    fun `should find the whole line in document`() {
        val searchQuery = "I like to work in the garden but my sister hates to work in the garden."
        val searchResult = documentSearcherManager.search(searchQuery)

        // Complete flow
        searchResult.tryEmit(CompleteSearchResult())

        // Only TestC.txt contains search query
        val documentContainsSearchQuery = listOf(documentFilePaths[2])
        val expectedResult = generateExpectedResults(searchQuery, documentContainsSearchQuery)

        val result = mutableListOf<SearchResult>()
        runBlocking {
            searchResult
                .takeWhile { it !is CompleteSearchResult }
                .onEach { result.add(it) }
                .collect()
        }
        assertEquals(expectedResult.sortedBy { it.filePath }, result.sortedBy { it.filePath })
    }

    private fun generateExpectedResults(searchQuery: String, documentFilePaths: List<Path>): List<SearchResult> {
        val expectedResult = mutableListOf<SearchResult>()
        documentFilePaths.forEach { path ->
            var linesCounter = 0
            Files.lines(path).use { lines ->
                lines.forEach { line ->
                    val positions = line.allIndicesOf(searchQuery)
                    if (positions.isNotEmpty()) {
                        expectedResult.add(AddSearchResult(path.toAbsolutePath().toString(), linesCounter, positions))
                    }

                    linesCounter++
                }
            }
        }
        return expectedResult
    }

}
