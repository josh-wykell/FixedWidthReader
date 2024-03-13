package fwreader.app

import javax.swing.*
import javax.swing.table.DefaultTableModel

class FileView {
    List<String>       headers
    List<List<String>> rows

    FileView(List headers, List rows) {
        this.headers = headers
        this.rows    = rows
    }

    void createView() {
        SwingUtilities.invokeLater {
            def frame = new JFrame("Table Display Example")

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
