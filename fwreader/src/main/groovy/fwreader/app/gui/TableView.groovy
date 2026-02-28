package fwreader.app.gui

import fwreader.app.Reader
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import java.awt.Component
import java.awt.Font

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

            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.setSize(400, 300)

            // Create menu bar
            def menuBar = new JMenuBar()
            def fileMenu = new JMenu("File")
            def openNewItem = new JMenuItem("Open New File...")

            openNewItem.addActionListener { event ->
                Thread.start {
                    openNewFile()
                }
            }

            fileMenu.add(openNewItem)
            menuBar.add(fileMenu)
            frame.setJMenuBar(menuBar)

            def tableModel = new DefaultTableModel(rows as Object[][], headers as Object[])
            def dataTable  = new JTable(tableModel)

            // Style headers: bold, larger font, centered
            def headerRenderer = new DefaultTableCellRenderer() {
                @Override
                Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    def component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    def baseFont = component.getFont()
                    component.setFont(new Font(baseFont.name, Font.BOLD, baseFont.size + 2))
                    setHorizontalAlignment(SwingConstants.CENTER)
                    return component
                }
            }
            dataTable.tableHeader.defaultRenderer = headerRenderer

            // Center cell text
            def centerRenderer = new DefaultTableCellRenderer()
            centerRenderer.horizontalAlignment = SwingConstants.CENTER
            for (int i = 0; i < dataTable.columnCount; i++) {
                dataTable.columnModel.getColumn(i).cellRenderer = centerRenderer
            }

            def scrollPane = new JScrollPane(dataTable)

            // Add padding
            ((JPanel) frame.contentPane).border = new EmptyBorder(10, 10, 10, 10)

            frame.add(scrollPane)
            frame.setVisible(true)
        }
    }

    private void openNewFile() {
        try {
            InputView input = new InputView()
            def files = input.selectFiles()
            File fwFile   = files["fwFile"]
            File fwConfig = files["configFile"]

            Reader fwReader = new Reader(fwFile, fwConfig)
            def headers = fwReader.getHeaders()
            def rows    = fwReader.collectRows()

            TableView tableViewer = new TableView(headers, rows)
            tableViewer.createView(fwFile.name)
        } catch (Exception e) {
            SwingUtilities.invokeLater {
                def retry = JOptionPane.showConfirmDialog(null,
                    "An error occurred while reading the file:\n\n${e.message}\n\nWould you like to try again?",
                    "Error",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE)

                if (retry == JOptionPane.YES_OPTION) {
                    openNewFile()
                }
            }
        }
    }
}
