package com.anleonov.app

import com.anleonov.app.component.JSearchResultTable
import com.anleonov.app.listener.SearchActionListener
import com.anleonov.app.model.SearchResultTableModel
import com.anleonov.app.worker.IndexingWorker
import com.anleonov.indexer.api.DocumentIndexer
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class SearchApplicationFrame(
    private val documentIndexer: DocumentIndexer
) : JFrame() {

    private val logger = LoggerFactory.getLogger(SearchApplicationFrame::class.java)

    // UI components for application menu
    private lateinit var indexFolderMenuItem: JMenuItem

    // UI components for indexing panel
    private lateinit var cancelIndexingButton: JButton
    private lateinit var progressBarIndexingPanel: JPanel
    private lateinit var progressIndexingLabel: JLabel
    private lateinit var progressIndexingBar: JProgressBar

    // Workers
    private lateinit var indexingWorker: IndexingWorker

    init {
        initUIComponents()
    }

    private fun initUIComponents() {
        title = "Search application"
        size = Dimension(800, 600)
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        initMenuBar()
        initProgressBarPanel()

        initMainUIPanel()
    }

    private fun initMenuBar() {
        val menuBar = JMenuBar()

        val indexingMenu = JMenu("Indexing")

        indexFolderMenuItem = JMenuItem("Index folder")
        indexFolderMenuItem.addActionListener {
            val folderChooser = JFileChooser()
            folderChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            folderChooser.dialogTitle = "Select folder for indexing"
            if (folderChooser.showDialog(this, "Index") == JFileChooser.APPROVE_OPTION) {
                val selectedPath = folderChooser.selectedFile.path
                logger.info("Selected folder path for indexing $selectedPath")
                indexingWorker = IndexingWorker(
                    progressBarIndexingPanel = progressBarIndexingPanel,
                    progressIndexingBar = progressIndexingBar,
                    progressIndexingLabel = progressIndexingLabel,
                    indexFolderMenuItem = indexFolderMenuItem,
                    folderPath = selectedPath,
                    documentIndexer = documentIndexer
                )
                documentIndexer.addIndexerListener(indexingWorker)
                indexFolderMenuItem.isEnabled = false
                progressBarIndexingPanel.isVisible = true
                progressIndexingBar.value = 0
                progressIndexingLabel.text = "Prepare for indexing"
                indexingWorker.execute()
            }
        }
        indexingMenu.add(indexFolderMenuItem)

        menuBar.add(indexingMenu)

        jMenuBar = menuBar
    }

    private fun initProgressBarPanel() {
        progressBarIndexingPanel = JPanel(BorderLayout())
        progressBarIndexingPanel.isVisible = false

        progressIndexingLabel = JLabel()
        progressBarIndexingPanel.add(progressIndexingLabel, BorderLayout.WEST)

        progressIndexingBar = JProgressBar()
        progressIndexingBar.isStringPainted = true
        progressBarIndexingPanel.add(progressIndexingBar, BorderLayout.CENTER)

        cancelIndexingButton = JButton("Cancel")
        cancelIndexingButton.addActionListener {
            val cancelIndexingWorker = object : SwingWorker<Any?, Any>() {
                override fun doInBackground(): Any? {
                    indexingWorker.cancel()
                    return null
                }
            }
            cancelIndexingWorker.execute()
        }
        progressBarIndexingPanel.add(cancelIndexingButton, BorderLayout.EAST)
    }

    private fun initMainUIPanel() {
        val mainPanel = JPanel(BorderLayout(5, 5))
        mainPanel.border = BorderFactory.createEmptyBorder(5, 10, 10, 10)

        mainPanel.add(createSearchPanel(), BorderLayout.PAGE_START)

        val tableModel = SearchResultTableModel()
        val searchResultTable = JSearchResultTable(tableModel)
        searchResultTable.fillsViewportHeight = true
        searchResultTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val scrollPane = JScrollPane(
            searchResultTable,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )

        mainPanel.add(scrollPane, BorderLayout.CENTER)

        val indexingBarPanel = JPanel()
        indexingBarPanel.layout = BoxLayout(indexingBarPanel, BoxLayout.Y_AXIS)
        indexingBarPanel.add(progressBarIndexingPanel)

        mainPanel.add(indexingBarPanel, BorderLayout.PAGE_END)

        contentPane.add(mainPanel)
    }

    private fun createSearchPanel(): JPanel {
        val searchPanel = JPanel(BorderLayout())

        val searchButton = JButton("Search")
        searchButton.addActionListener(SearchActionListener())
        searchPanel.add(searchButton, BorderLayout.EAST)

        val searchField = JTextField()
        searchPanel.add(searchField, BorderLayout.CENTER)

        return searchPanel
    }

}
