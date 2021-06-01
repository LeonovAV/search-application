package com.anleonov.app.worker

import com.anleonov.app.component.JSearchResultTable
import com.anleonov.app.model.SearchResultRow
import com.anleonov.searcher.api.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import javax.swing.SwingWorker

/**
 * Class is responsible for background searching based on query. It extends
 * SwingWorker in order not to block main UI thread. Also it uses a coroutine
 * to keep search results up to date with file system changes.
 */
class SearchWorker(
    private val searchQuery: String,
    private val documentSearcher: DocumentSearcher,
    private val searchResultTable: JSearchResultTable
) : SwingWorker<MutableSharedFlow<SearchResult>, Any>() {

    private val logger = LoggerFactory.getLogger(SearchWorker::class.java)

    override fun doInBackground(): MutableSharedFlow<SearchResult> {
        return documentSearcher.search(searchQuery)
    }

    override fun done() {
        processSearchResult()
    }

    private fun processSearchResult() {
        try {
            searchResultTable.selectionModel.clearSelection()
            val model = searchResultTable.model
            model.resetResultRows()

            val searchResults = get()
            GlobalScope.launch {
                searchResults
                    .takeWhile { it !is CompleteSearchResult }
                    .onEach { searchResult ->
                        val selectedRowIndex = searchResultTable.selectedRow
                        when (searchResult) {
                            is AddSearchResult -> {
                                model.addNewRow(searchResult.toViewModel())
                            }
                            is RemoveSearchResult -> {
                                val deletedIndex = model.removeRow(searchResult.filePath, searchResult.lineNumber)
                                if (deletedIndex <= selectedRowIndex) {
                                    searchResultTable.clearSelection()
                                }
                            }
                            is UpdateSearchResult -> {
                                val updatedIndex = model.updateRow(searchResult.filePath, searchResult.lineNumber, searchResult.positions)
                                if (updatedIndex != -1 && updatedIndex == selectedRowIndex) {
                                    searchResultTable.clearSelection()
                                }
                            }
                            else -> {
                                logger.debug("Get complete event for search results")
                            }
                        }
                    }
                    .catch { logger.error("An exception occurred during processing search result", it) }
                    .collect()
            }
        } catch (ex: Exception) {
            when (ex) {
                is InterruptedException, is ExecutionException, is CancellationException -> {
                    logger.warn("Search worker task is cancelled")
                }
                else -> throw ex
            }
        }
    }

    private fun SearchResult.toViewModel(): SearchResultRow {
        return SearchResultRow(this.filePath, this.lineNumber, this.positions)
    }

}
