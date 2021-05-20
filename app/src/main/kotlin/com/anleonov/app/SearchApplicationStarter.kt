package com.anleonov.app

import com.anleonov.indexer.DocumentIndexerManager
import com.anleonov.indexer.filesystem.FileSystemEntryRegistry
import com.anleonov.indexer.filesystem.FileSystemTracker
import java.nio.file.FileSystems

fun main() {
    val watchService = FileSystems.getDefault().newWatchService()
    val fileSystemRegistry = FileSystemEntryRegistry(watchService)
    val fileSystemTracker = FileSystemTracker(fileSystemRegistry)
    val documentIndexer = DocumentIndexerManager(fileSystemTracker)

    val searchApplicationFrame = SearchApplicationFrame(documentIndexer)
    searchApplicationFrame.isVisible = true
}
