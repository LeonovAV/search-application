package com.anleonov.app

import com.anleonov.index.DocumentIndexStore
import com.anleonov.index.tokenizer.NGramTokenizer
import com.anleonov.indexer.DocumentIndexerManager
import com.anleonov.indexer.filesystem.FileSystemEntryRegistry
import com.anleonov.indexer.filesystem.FileSystemTracker
import java.nio.file.FileSystems

fun main() {
    val watchService = FileSystems.getDefault().newWatchService()
    val fileSystemRegistry = FileSystemEntryRegistry(watchService)
    val fileSystemTracker = FileSystemTracker(fileSystemRegistry)

    val tokenizer = NGramTokenizer()
    val documentIndex = DocumentIndexStore()

    val documentIndexer = DocumentIndexerManager(tokenizer, documentIndex, fileSystemTracker)

    val searchApplicationFrame = SearchApplicationFrame(documentIndexer)
    searchApplicationFrame.isVisible = true
}
