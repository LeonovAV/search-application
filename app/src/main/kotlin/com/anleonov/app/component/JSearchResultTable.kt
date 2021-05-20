package com.anleonov.app.component

import com.anleonov.app.model.SearchResultTableModel
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel

class JSearchResultTable(
    tableModel: TableModel
) : JTable(tableModel) {

    init {
        setAutoResizeMode(AUTO_RESIZE_OFF)
    }

    override fun prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int): Component {
        val component = super.prepareRenderer(renderer, row, column)
        val rendererWidth = component.preferredSize.width
        val tableColumn = getColumnModel().getColumn(column)
        tableColumn.preferredWidth = (rendererWidth + intercellSpacing.width).coerceAtLeast(tableColumn.preferredWidth)
        return component
    }

    override fun getModel(): SearchResultTableModel {
        return super.getModel() as SearchResultTableModel
    }

}
