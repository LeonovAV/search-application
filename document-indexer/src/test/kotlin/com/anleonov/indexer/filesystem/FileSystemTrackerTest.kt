package com.anleonov.indexer.filesystem

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.FileSystems
import java.nio.file.Paths

class FileSystemTrackerTest {

    private val watchService = FileSystems.getDefault().newWatchService()
    private val fileSystemEntryRegistry = FileSystemEntryRegistry(watchService)

    private val fileSystemTracker = FileSystemTracker(fileSystemEntryRegistry)

    @After
    fun tearDown() {
        watchService.close()
    }

    @Test
    fun `should register and track new existing folder`() {
        val folderPath = Paths.get("src", "test", "resources", "./TestFolderA")
        val result = fileSystemTracker.registerFolder(folderPath)
        assertTrue(result)
    }

    @Test
    fun `should register and track new folder in existing folder`() {
        val folderPath = Paths.get("src", "test", "resources", "TestFolderB")
        assertTrue(fileSystemTracker.registerFolder(folderPath))

        // create folder and register it
        val testPath = Paths.get("src", "test", "resources", "TestFolderB", "TrackedFolder")
        val testFile = testPath.toFile()

        assertTrue(testFile.mkdir())

        assertTrue(fileSystemTracker.registerFolder(testPath))

        // delete folder
        assertTrue(testFile.delete())
    }

}
