package com.anleonov.indexer.filesystem

import com.anleonov.indexer.model.FileSystemEventType
import com.anleonov.indexer.model.FileSystemEventType.*
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.util.concurrent.CopyOnWriteArrayList

class FileSystemEventNotificationTask(
    private val fileSystemEntryRegistry: FileSystemEntryRegistry
) : Runnable {

    private val logger = LoggerFactory.getLogger(FileSystemEventNotificationTask::class.java)

    private val listeners = CopyOnWriteArrayList<FileSystemEventListener>()

    override fun run() {
        try {
            val key = fileSystemEntryRegistry.getNextWatchKey()
            if (key != null) {
                val pathByKey = fileSystemEntryRegistry.findPathByWatchKey(key)
                if (pathByKey != null) {
                    key.pollEvents().forEach { event ->
                        val path = pathByKey.resolve(event.context() as Path)
                        val eventKind = event.kind()

                        logger.debug("Get event with type $eventKind for $path")

                        when (eventKind) {
                            ENTRY_DELETE -> {
                                notifyListeners(path, DELETED) { fileSystemEntryRegistry.containsFolder(path) }
                            }
                            ENTRY_CREATE -> {
                                notifyListeners(path, CREATED) { Files.isDirectory(path) }
                            }
                            ENTRY_MODIFY -> {
                                notifyListeners(path, MODIFIED) { Files.isDirectory(path) }
                            }
                        }
                    }
                    key.reset()
                }
            }
        } catch (e: InterruptedException) {
            logger.warn("Could not get events from watch service", e)
        }
    }

    fun addListener(listener: FileSystemEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: FileSystemEventListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(
        path: Path,
        fileSystemEventType: FileSystemEventType,
        isFolderPredicate: (path: Path) -> Boolean
    ) {
        if (isFolderPredicate.invoke(path)) {
            listeners.forEach { it.onFolderChanged(path, fileSystemEventType) }
        } else {
            listeners.forEach { it.onFileChanged(path, fileSystemEventType) }
        }
    }

}
