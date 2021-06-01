package com.anleonov.indexer.filesystem

import com.anleonov.indexer.model.FileSystemEventType
import java.nio.file.Path

/**
 * This interface allows one to get events from the filesystem about
 * changes in the registered entities.
 */
interface FileSystemEventListener {

    /**
     * Method for handling folder changes events
     *
     * @param folderPath path to the changed folder
     * @param type event type of file change
     */
    fun onFolderChanged(folderPath: Path, type: FileSystemEventType)

    /**
     * Method for handling file changes events coming from the filesystem
     *
     * @param event Event of that type of action happened with file
     * @param filePath Path to the changed file
     */
    fun onFileChanged(filePath: Path, type: FileSystemEventType)

}
