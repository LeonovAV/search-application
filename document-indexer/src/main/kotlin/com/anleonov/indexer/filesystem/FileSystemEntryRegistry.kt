package com.anleonov.indexer.filesystem

import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.concurrent.ConcurrentHashMap

class FileSystemEntryRegistry(
    private val watchService: WatchService
) {

    private val logger = LoggerFactory.getLogger(FileSystemEntryRegistry::class.java)

    private val registeredFolders = ConcurrentHashMap<Path, WatchKey>()

    private val trackedFolders = ConcurrentHashMap.newKeySet<Path>()
    private val trackedFiles = ConcurrentHashMap.newKeySet<Path>()

    fun registerFolder(folderPath: Path, shouldTrackFolder: Boolean = true): Boolean {
        logger.debug("Try to register $folderPath folder")
        var isRegistered = false
        try {
            if (!registeredFolders.containsKey(folderPath)) {
                val watchKey = folderPath.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                registeredFolders[folderPath] = watchKey
                isRegistered = true
            } else {
                logger.debug("Folder $folderPath is already registered")
            }
            if (!trackedFolders.contains(folderPath) && shouldTrackFolder) {
                trackedFolders.add(folderPath)
            }
        } catch (e: IOException) {
            logger.warn("Could not register folder", e)
        }
        return isRegistered
    }

    fun registerFile(filePath: Path): Boolean {
        logger.debug("Try to register $filePath file")
        val isAdded = trackedFiles.add(filePath)
        if (!isAdded) {
            logger.debug("File $filePath is already registered")
        }
        return isAdded
    }

    fun unregisterFolder(folderPath: Path): Boolean {
        logger.debug("Unregister folder $folderPath with sub-directories")
        val foldersToRemove = trackedFolders.filter { it.startsWith(folderPath) }
        var isRemoved = false
        foldersToRemove.forEach {
            trackedFolders.remove(it)
            registeredFolders[it]?.let { key ->
                registeredFolders.remove(it)
                key.cancel()
                isRemoved = true
            }
        }
        return isRemoved
    }

    fun unregisterFile(filePath: Path): Boolean {
        return trackedFiles.remove(filePath)
    }

    fun findPathByWatchKey(key: WatchKey): Path? {
        return registeredFolders.entries.find { (_, value) -> value == key }?.key
    }

    fun getNextWatchKey(): WatchKey? {
        return watchService.take()
    }

    fun containsFolder(folderPath: Path?): Boolean {
        return trackedFolders.contains(folderPath)
    }

    fun containsFile(filePath: Path?): Boolean {
        return trackedFiles.contains(filePath)
    }

    fun getRegisteredFiles(): List<Path> {
        return trackedFiles.toList()
    }

    fun clear() {
        trackedFiles.clear()
        trackedFolders.clear()
        registeredFolders.forEach { (_, value) -> value.cancel() }
        registeredFolders.clear()
    }

}
