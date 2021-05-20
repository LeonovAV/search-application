package com.anleonov.indexer.filesystem

import com.anleonov.indexer.model.FileSystemEventType
import java.nio.file.Path

interface FileSystemEventListener {

    fun onFolderChanged(folderPath: Path, type: FileSystemEventType)

    fun onFileChanged(filePath: Path, type: FileSystemEventType)

}
