package fwreader.app.gui

import javax.swing.*
import javax.swing.table.DefaultTableModel

class TableView {
    List<String>       headers
    List<List<String>> rows

    TableView(List headers, List rows) {
        this.headers = headers
        this.rows    = rows
    }

    void createView(String viewName) {
        SwingUtilities.invokeLater {
            def frame = new JFrame("${viewName}")

            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(400, 300)

            def tableModel = new DefaultTableModel(rows as Object[][], headers as Object[])
            def table      = new JTable(tableModel)
            def scrollPane = new JScrollPane(table)

            frame.add(scrollPane)
            frame.setVisible(true)
        }
    }
}
