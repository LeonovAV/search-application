package com.anleonov.app

import com.anleonov.index.DocumentIndexStore
import com.anleonov.index.DocumentStoreImpl
import com.anleonov.index.api.CommonNGramSize.triGram
import com.anleonov.index.tokenizer.NGramTokenizer
import com.anleonov.indexer.DocumentIndexerManager
import com.anleonov.indexer.filesystem.FileSystemEntryRegistry
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.searcher.DocumentSearcherManager
import java.nio.file.FileSystems

fun main() {
    val watchService = FileSystems.getDefault().newWatchService()
    val fileSystemRegistry = FileSystemEntryRegistry(watchService)
    val fileSystemTracker = FileSystemTracker(fileSystemRegistry)

    val tokenizer = NGramTokenizer(triGram)
    val documentIndex = DocumentIndexStore()
    val documentStore = DocumentStoreImpl()

    val documentIndexer = DocumentIndexerManager(tokenizer, documentIndex, documentStore, fileSystemTracker)
    val documentSearcher = DocumentSearcherManager(tokenizer, documentIndex, documentStore)

    val searchApplicationFrame = SearchApplicationFrame(documentIndexer, documentSearcher)
    searchApplicationFrame.isVisible = true
}
