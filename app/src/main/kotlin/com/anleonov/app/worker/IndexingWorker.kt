package com.anleonov.app.worker

import com.anleonov.indexer.api.DocumentIndexer
import com.anleonov.indexer.api.DocumentIndexerListener
import org.slf4j.LoggerFactory
import javax.swing.*

/**
 *
 */
class IndexingWorker(
    private val progressBarIndexingPanel: JPanel,
    private val progressIndexingBar: JProgressBar,
    private val progressIndexingLabel: JLabel,
    private val indexFolderMenuItem: JMenuItem,
    private val folderPath: String,
    private val documentIndexer: DocumentIndexer
) : SwingWorker<Any?, Int>(), DocumentIndexerListener {

    private val logger = LoggerFactory.getLogger(IndexingWorker::class.java)

    override fun doInBackground(): Any? {
        logger.info("Start background indexing job")
        documentIndexer.indexFolder(folderPath)
        return null
    }

    override fun process(chunks: MutableList<Int>) {
        chunks.forEach {
            // from 0 to 99 - progress for indexing
            if (it in 0..99) {
                if (!progressBarIndexingPanel.isVisible && !isCancelled) {
                    progressBarIndexingPanel.isVisible = true
                }
                progressIndexingLabel.text = "Indexing..."
                progressIndexingBar.value = it
            } else if (it == -1) {
                indexFolderMenuItem.isEnabled = true
                progressIndexingLabel.text = "Indexing finished"
                progressBarIndexingPanel.isVisible = false
            }
        }
    }

    fun cancel() {
        logger.info("Cancelling...")
        super.cancel(true)
        documentIndexer.cancelIndexingFolder()
    }

    override fun onIndexingInProgress(progress: Int) {
        publish(progress)
    }

    override fun onIndexingFinished() {
        publish(-1)
    }

}