package com.anleonov.indexer

import com.anleonov.index.api.Document
import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.DocumentStore
import com.anleonov.index.api.Tokenizer
import com.anleonov.indexer.api.DocumentIndexer
import com.anleonov.indexer.api.DocumentIndexerListener
import com.anleonov.indexer.executor.ExecutorsProvider
import com.anleonov.indexer.filesystem.FileSystemEventListener
import com.anleonov.indexer.filesystem.FileSystemTracker
import com.anleonov.indexer.model.FileSystemEventType
import com.anleonov.indexer.model.FileSystemEventType.*
import com.anleonov.indexer.model.IndexingEvent
import com.anleonov.indexer.task.*
import com.anleonov.indexer.util.DocumentIdGenerator
import com.anleonov.indexer.util.supportedFileExtensions
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Class is responsible for indexing folders or files. It also handles events coming from
 * file system tracker. Each action starts a separate task to handle actions independently
 * and to speed up indexing process.
 */
class DocumentIndexerManager(
    private val tokenizer: Tokenizer,
    private val documentIndex: DocumentIndex,
    private val documentStore: DocumentStore,
    private val fileSystemTracker: FileSystemTracker
) : DocumentIndexer, FileSystemEventListener {

    private val logger = LoggerFactory.getLogger(DocumentIndexerManager::class.java)

    private val listeners = CopyOnWriteArrayList<DocumentIndexerListener>()

    // thread pool for reading files and producing events for indexing
    private val indexingExecutorService = ExecutorsProvider.executorService

    // thread pool for consuming indexing events
    private val indexingScheduledExecutorService = ExecutorsProvider.scheduledExecutorService

    private val queueCapacity = 100_000
    private val indexingEventsQueue = ArrayBlockingQueue<IndexingEvent>(queueCapacity)

    // status of current indexing process
    @Volatile
    private var isCurrentIndexingCancelled = false
    private var currentIndexingTasks = mutableListOf<Future<*>>()

    init {
        val indexingJob = DocumentIndexingJob(tokenizer, documentIndex, indexingEventsQueue, listeners)
        indexingScheduledExecutorService.scheduleWithFixedDelay(indexingJob, 0, 1, TimeUnit.SECONDS)

        fileSystemTracker.addListener(this)
    }

    override fun indexFolder(path: String) {
        if (path.isEmpty()) {
            throw IllegalArgumentException("Path for indexing must not be empty")
        }
        val normalizedPath = Paths.get(path).normalize()

        if (hasAccess(normalizedPath)) {
            logger.info("Try to index folder $normalizedPath")

            val filesCountForIndexing = getFilesCount(normalizedPath)
            logger.info("Files count for indexing $filesCountForIndexing")

            val percentage = filesCountForIndexing / 100.0
            val documentCounter = AtomicInteger(0)

            // re-initialize current indexing state
            isCurrentIndexingCancelled = false
            currentIndexingTasks = mutableListOf()

            try {
                Files.walkFileTree(normalizedPath, object : SimpleFileVisitor<Path>() {

                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        indexFileWithProgress(file, documentCounter, percentage)
                        return getFileVisitResult()
                    }

                    override fun preVisitDirectory(path: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult {
                        logger.debug("Pre visit directory $path")
                        return if (Files.isHidden(path)) {
                            FileVisitResult.SKIP_SUBTREE
                        } else {
                            fileSystemTracker.registerFolder(path)
                            FileVisitResult.CONTINUE
                        }
                    }

                    private fun getFileVisitResult(): FileVisitResult {
                        return if (isCurrentIndexingCancelled) {
                            logger.debug("Terminate indexing due to cancelling")
                            FileVisitResult.TERMINATE
                        } else FileVisitResult.CONTINUE
                    }

                })
            } catch (e: IOException) {
                logger.warn("Indexing failed", e)
            }
        } else {
            logger.warn("No read access to folder $normalizedPath")
            listeners.forEach { it.onIndexingFinished() }
        }
    }

    override fun cancelIndexingFolder() {
        logger.info("Cancel indexing folder")

        isCurrentIndexingCancelled = true

        currentIndexingTasks.forEach {
            if (!it.isDone) {
                it.cancel(true)
            }
        }

        indexingEventsQueue.clear()

        documentIndex.clear()
        documentStore.clear()

        fileSystemTracker.clear()

        DocumentIdGenerator.reset()
    }

    override fun addIndexerListener(listener: DocumentIndexerListener) {
        listeners.add(listener)
    }

    override fun removeIndexerListener(listener: DocumentIndexerListener) {
        listeners.remove(listener)
    }

    override fun onFolderChanged(folderPath: Path, type: FileSystemEventType) {
        logger.debug("Handle event type $type for folder: $folderPath")
        when (type) {
            CREATED -> {
                indexCreatedFolder(folderPath)
            }
            DELETED -> {
                fileSystemTracker.unregisterFolder(folderPath)
                val documentsToBeDeleted = documentStore.findDocumentsStartsWith(folderPath)
                documentsToBeDeleted.forEach { document ->
                    removeDocumentFromIndex(document)
                    fileSystemTracker.unregisterFolder(document.parentPath)
                }
            }
            MODIFIED -> {
                logger.debug("Processing of folder modified event is skipped")
            }
        }
    }

    override fun onFileChanged(filePath: Path, type: FileSystemEventType) {
        logger.debug("Handle event type $type for file $filePath")
        when (type) {
            CREATED -> {
                indexFile(filePath)
            }
            DELETED -> {
                documentStore.getDocumentByPath(filePath)?.let {
                    removeDocumentFromIndex(it)
                }
            }
            MODIFIED -> {
                if (isFileIndexed(filePath)) {
                    reindexFile(filePath)
                }
            }
        }
    }

    private fun indexCreatedFolder(folderPath: Path) {
        if (hasAccess(folderPath)) {
            logger.debug("Index new created folder $folderPath")

            try {
                Files.walkFileTree(folderPath, object : SimpleFileVisitor<Path>() {

                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        indexFile(file)
                        return FileVisitResult.CONTINUE
                    }

                    override fun preVisitDirectory(path: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult {
                        return if (Files.isHidden(path)) {
                            FileVisitResult.SKIP_SUBTREE
                        } else {
                            fileSystemTracker.registerFolder(path)
                            FileVisitResult.CONTINUE
                        }
                    }

                })
            } catch (e: IOException) {
                logger.warn("Indexing of folder $folderPath failed ", e)
            }
        }
    }

    private fun indexFileWithProgress(filePath: Path, documentCounter: AtomicInteger, percentage: Double) {
        if (hasAccess(filePath) && isFileAvailable(filePath) && !isFileIndexed(filePath)) {
            val document = Document(DocumentIdGenerator.generate(), filePath, filePath.parent)
            val documentNumber = documentCounter.incrementAndGet()
            val task = ReadDocumentWithProgressTask(
                document,
                documentNumber,
                percentage,
                documentStore,
                fileSystemTracker,
                indexingEventsQueue,
                listeners
            )
            val submittedTask = indexingExecutorService.submit(task)
            currentIndexingTasks.add(submittedTask)
        }
    }

    private fun indexFile(filePath: Path) {
        if (hasAccess(filePath) && isFileAvailable(filePath) && !isFileIndexed(filePath)) {
            logger.debug("Index new created file $filePath")
            val document = Document(DocumentIdGenerator.generate(), filePath, filePath.parent)
            val task = ReadDocumentTask(
                document,
                documentStore,
                fileSystemTracker,
                indexingEventsQueue
            )
            val submittedTask = indexingExecutorService.submit(task)
            currentIndexingTasks.add(submittedTask)
        }
    }

    private fun removeDocumentFromIndex(document: Document) {
        logger.debug("Remove file ${document.path} from index")
        val task = RemoveDocumentTask(
            document,
            documentIndex,
            documentStore,
            fileSystemTracker,
            indexingEventsQueue
        )
        indexingExecutorService.execute(task)
    }

    private fun reindexFile(filePath: Path) {
        if (hasAccess(filePath) && isFileAvailable(filePath)) {
            logger.debug("Re-index file $filePath due to modification")

            documentStore.getDocumentByPath(filePath)?.let {
                val task = UpdateDocumentTask(it, tokenizer, documentIndex, indexingEventsQueue)
                indexingExecutorService.submit(task)
            }
        }
    }

    private fun hasAccess(path: Path): Boolean {
        return Files.exists(path) && Files.isReadable(path)
    }

    private fun getFilesCount(path: Path): Long {
        var count: Long = 0
        Files.newDirectoryStream(path).use { directoryStream ->
            directoryStream.forEach {
                if (isFolderAvailable(it)) {
                    count += getFilesCount(it)
                } else if (isFileAvailable(it)) {
                    count++
                }
            }
        }
        return count
    }

    private fun isFileIndexed(filePath: Path): Boolean {
        return documentStore.containsDocument(filePath)
    }

    private fun isFolderAvailable(path: Path): Boolean {
        return Files.isDirectory(path) && !Files.isHidden(path) && Files.isReadable(path)
    }

    private fun isFileAvailable(path: Path): Boolean {
        return Files.isRegularFile(path) && Files.isReadable(path) && path.toFile().extension.toLowerCase() in supportedFileExtensions
    }

}
