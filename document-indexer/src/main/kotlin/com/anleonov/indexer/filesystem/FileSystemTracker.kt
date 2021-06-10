package com.anleonov.indexer.filesystem

import com.anleonov.indexer.executor.ExecutorsProvider
import com.anleonov.indexer.model.FileSystemEventType
import com.anleonov.indexer.model.FileSystemEventType.*
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

class FileSystemTracker(
    private val fileSystemEntryRegistry: FileSystemEntryRegistry
) : FileSystemEventListener {

    private val fileTrackerScheduledExecutorService = ExecutorsProvider.scheduledExecutorService

    private val listeners = CopyOnWriteArrayList<FileSystemEventListener>()

    init {
        logger.info("Initialize file system notification task")

        val task = FileSystemEventNotificationTask(fileSystemEntryRegistry)
        fileTrackerScheduledExecutorService.scheduleWithFixedDelay(task, 0, 2, TimeUnit.SECONDS)

        task.addListener(this)
    }

    fun registerFolder(folderPath: Path): Boolean {
        return fileSystemEntryRegistry.registerFolder(folderPath)
    }

    fun registerFile(filePath: Path): Boolean {
        val isFileRegistered = fileSystemEntryRegistry.registerFile(filePath)
        if (isFileRegistered) {
            val fileParentPath = filePath.parent

            // Register parent of parent in order to track folder deletion
            if (fileParentPath.parent != null) {
                fileSystemEntryRegistry.registerFolder(fileParentPath.parent, false)
            }

            fileSystemEntryRegistry.registerFolder(fileParentPath, false)
        }
        return isFileRegistered
    }

    fun unregisterFolder(folderPath: Path): Boolean {
        return fileSystemEntryRegistry.unregisterFolder(folderPath)
    }

    fun unregisterFile(filePath: Path): Boolean {
        return fileSystemEntryRegistry.unregisterFile(filePath)
    }

    fun addListener(listener: FileSystemEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: FileSystemEventListener) {
        listeners.remove(listener)
    }

    fun clear() {
        fileSystemEntryRegistry.clear()
    }

    override fun onFolderChanged(folderPath: Path, type: FileSystemEventType) {
        when (type) {
            CREATED -> {
                if (fileSystemEntryRegistry.containsFolder(folderPath.parent)) {
                    // Sub-folder with tracked parent - should index it
                    listeners.forEach { it.onFolderChanged(folderPath, type) }
                }
            }
            DELETED -> {
                if (fileSystemEntryRegistry.containsFolder(folderPath)) {
                    unregisterFolder(folderPath)
                } else {
                    // Folder itself is not tracked, but files are tracked - should be deleted
                    fileSystemEntryRegistry.getRegisteredFiles().forEach { filePath ->
                        if (Files.isSameFile(filePath.parent, folderPath)) {
                            onFileChanged(filePath, type)
                        }
                    }
                }
                listeners.forEach { it.onFolderChanged(folderPath, type) }
            }
            MODIFIED -> {
                if (fileSystemEntryRegistry.containsFolder(folderPath)) {
                    listeners.forEach { it.onFolderChanged(folderPath, type) }
                }
            }
        }
    }

    override fun onFileChanged(filePath: Path, type: FileSystemEventType) {
        val parentFolderPath = filePath.parent
        val isTrackedFolder = fileSystemEntryRegistry.containsFolder(parentFolderPath)
        val isTrackedFile = fileSystemEntryRegistry.containsFile(filePath)

        if (isTrackedFile || isTrackedFolder) {
            listeners.forEach { it.onFileChanged(filePath, type) }
        }
    }

}
