package com.anleonov.app.model

import javax.swing.table.AbstractTableModel

/**
 *
 */
class SearchResultTableModel : AbstractTableModel() {

    private var resultRows = mutableListOf<SearchResultRow>()

    private val columnCount = 2

    fun addNewRow(row: SearchResultRow) {
        resultRows.add(row)
        fireTableRowsInserted(resultRows.size - 1, resultRows.size - 1)
    }

    fun removeRow(filePath: String, rowNumber: Int): Int {
        var indexToDelete = 0
        resultRows.forEachIndexed { index, row ->
            if (row.filePath == filePath && row.rowNumber == rowNumber) {
                indexToDelete = index
                return@forEachIndexed
            }
        }
        fireTableRowsDeleted(indexToDelete, indexToDelete)
        resultRows.removeAt(indexToDelete)
        return indexToDelete
    }

    fun updateRow(filePath: String, rowNumber: Int, positions: List<Int>): Int {
        resultRows.forEachIndexed { index, row ->
            if (row.filePath == filePath && row.rowNumber == rowNumber) {
                row.positions = positions
                return index
            }
        }
        return -1
    }

    fun resetResultRows() {
        resultRows = mutableListOf()
        fireTableDataChanged()
    }

    override fun getRowCount(): Int {
        return resultRows.size
    }

    override fun getColumnCount(): Int {
        return columnCount
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            0 -> "File path"
            1 -> "Row number"
            else -> throw IllegalArgumentException("Invalid column index $column, current column count $columnCount")
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        if (rowIndex >= resultRows.size) return ""

        return when (columnIndex) {
            0 -> {
                resultRows[rowIndex].filePath
            }
            1 -> {
                resultRows[rowIndex].rowNumber.toString()
            }
            else -> throw IllegalArgumentException("Invalid column index $columnIndex, current column count $columnCount")
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

}

/**
 *
 */
data class SearchResultRow(
    val filePath: String,
    val rowNumber: Int,
    var positions: List<Int>
)
