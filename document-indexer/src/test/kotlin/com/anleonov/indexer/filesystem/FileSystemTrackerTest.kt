package com.anleonov.indexer.filesystem

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE

class FileSystemTrackerTest {

//    private val watchService = FileSystems.getDefault().newWatchService()
//
//    private val fileSystemTracker = FileSystemTracker(watchService)
//
//    @After
//    fun tearDown() {
//        watchService.close()
//    }
//
//    @Test
//    fun `should register and track new existing folder`() {
//        val folderPath = Paths.get("src", "test", "resources", "./TestFolderA")
//        val result = fileSystemTracker.registerFolder(folderPath)
//        assertTrue(result)
//    }
//
//    @Test
//    fun `should register and track new folder in existing folder`() {
//        val folderPath = Paths.get("src", "test", "resources", "TestFolderB")
//        assertTrue(fileSystemTracker.registerFolder(folderPath))
//
//        // create folder and register it
//        val testPath = Paths.get("src", "test", "resources", "TestFolderB", "TrackedFolder")
//        val testFile = testPath.toFile()
//
//        assertTrue(testFile.mkdir())
//
//        val key = watchService.take()
//        val createdKind = key.pollEvents()[0].kind()
//        assertEquals(ENTRY_CREATE, createdKind)
//
//        key.reset()
//
//        assertTrue(fileSystemTracker.registerFolder(testPath))
//
//        // delete folder
//        assertTrue(testFile.delete())
//
//        val deletedKind = watchService.take().pollEvents()[0].kind()
//        assertEquals(ENTRY_DELETE, deletedKind)
//    }

}
