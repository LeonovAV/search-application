package com.anleonov.app.model

import javax.swing.table.AbstractTableModel

/**
 *
 */
class SearchResultTableModel : AbstractTableModel() {

    private val resultRows = mutableListOf<SearchResultRow>()

    private val columnCount = 2

    override fun getRowCount(): Int {
        return resultRows.size
    }

    override fun getColumnCount(): Int {
        return columnCount
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            1 -> "Row number"
            else -> "File path"
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        if (rowIndex >= resultRows.size) return ""

        return when (columnIndex) {
            1 -> {
                resultRows[rowIndex].rowNumber.toString()
            }
            2 -> {
                resultRows[rowIndex].filePath
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
    val positions: List<Int>
)
