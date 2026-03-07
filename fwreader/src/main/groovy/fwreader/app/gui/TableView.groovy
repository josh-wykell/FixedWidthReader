package fwreader.app.gui

import fwreader.app.Reader
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.RowFilter
import javax.swing.table.TableRowSorter

class TableView {
    List<String>       headers
    List<List<String>> rows
    List<Integer>      columnWidths

    TableView(List headers, List rows, List<Integer> columnWidths) {
        this.headers      = headers
        this.rows         = rows
        this.columnWidths = columnWidths
    }

    void createView(String viewName) {
        SwingUtilities.invokeLater {
          try {
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

            // One shared model; each table shows a different subset of columns
            def allHeaders = (["#"] + headers) as Object[]
            def allData    = rows.withIndex(1).collect { row, i -> ([i] + row) as Object[] } as Object[][]

            def sharedModel = new DefaultTableModel(allData, allHeaders) {
                @Override boolean isCellEditable(int r, int c) { false }
            }

            // Frozen table: keep only the # column (column 0)
            def frozenTable = new JTable(sharedModel)
            frozenTable.autoResizeMode = JTable.AUTO_RESIZE_OFF
            while (frozenTable.columnCount > 1) {
                frozenTable.removeColumn(frozenTable.columnModel.getColumn(1))
            }

            // Scrollable table: remove the # column so data columns start at view index 0
            def dataTable = new JTable(sharedModel)
            dataTable.autoResizeMode = JTable.AUTO_RESIZE_OFF
            dataTable.removeColumn(dataTable.columnModel.getColumn(0))

            // Sync row selection so clicking highlights the same row in both tables
            dataTable.selectionModel = frozenTable.selectionModel

            // Shared header renderer: bold, larger, blue background, bordered
            def headerRenderer = new DefaultTableCellRenderer() {
                @Override
                Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    def component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    def baseFont = component.getFont()
                    component.setFont(new Font(baseFont.name, Font.BOLD, baseFont.size + 2))
                    component.setBackground(new Color(200, 215, 245))
                    setHorizontalAlignment(SwingConstants.CENTER)
                    setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.LIGHT_GRAY))
                    return component
                }
            }
            dataTable.tableHeader.defaultRenderer   = headerRenderer
            frozenTable.tableHeader.defaultRenderer = headerRenderer

            def ODD_ROW_COLOR  = new Color(240, 245, 255)
            def EVEN_ROW_COLOR = Color.WHITE
            // Column selection highlight — slightly deeper blue than the row tint
            def selectedModelCol      = [-1]
            def COLUMN_HIGHLIGHT_EVEN = new Color(195, 220, 255)
            def COLUMN_HIGHLIGHT_ODD  = new Color(180, 207, 248)

            // Apply grid lines to both tables
            for (t in [dataTable, frozenTable]) {
                t.showGrid         = true
                t.gridColor        = Color.LIGHT_GRAY
                t.intercellSpacing = new Dimension(1, 1)
            }

            // Frozen column: narrow, right-aligned, alternating + column highlight
            def rowNumRenderer = new DefaultTableCellRenderer() {
                @Override
                Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    def c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    if (!isSelected) {
                        boolean colSelected = table.convertColumnIndexToModel(column) == selectedModelCol[0]
                        c.background = colSelected
                            ? ((row % 2 == 0) ? COLUMN_HIGHLIGHT_EVEN : COLUMN_HIGHLIGHT_ODD)
                            : ((row % 2 == 0) ? EVEN_ROW_COLOR        : ODD_ROW_COLOR)
                    }
                    setHorizontalAlignment(SwingConstants.RIGHT)
                    return c
                }
            }
            frozenTable.columnModel.getColumn(0).cellRenderer   = rowNumRenderer
            frozenTable.columnModel.getColumn(0).minWidth       = 45
            frozenTable.columnModel.getColumn(0).preferredWidth = 45
            frozenTable.columnModel.getColumn(0).maxWidth       = 45

            // Data columns: centered, alternating + column highlight
            def centerRenderer = new DefaultTableCellRenderer() {
                @Override
                Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    def c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    if (!isSelected) {
                        boolean colSelected = table.convertColumnIndexToModel(column) == selectedModelCol[0]
                        c.background = colSelected
                            ? ((row % 2 == 0) ? COLUMN_HIGHLIGHT_EVEN : COLUMN_HIGHLIGHT_ODD)
                            : ((row % 2 == 0) ? EVEN_ROW_COLOR        : ODD_ROW_COLOR)
                    }
                    setHorizontalAlignment(SwingConstants.CENTER)
                    return c
                }
            }
            def fm     = dataTable.getFontMetrics(dataTable.getFont())
            def charPx = fm.charWidth((char) 'W')
            for (int i = 0; i < columnWidths.size(); i++) {
                def col = dataTable.columnModel.getColumn(i)
                col.cellRenderer   = centerRenderer
                col.preferredWidth = columnWidths[i] * charPx + 10
            }

            // Tell frozenScrollPane how wide to make its viewport — match column width exactly
            frozenTable.setPreferredScrollableViewportSize(new Dimension(45, 1))

            def frozenScrollPane = new JScrollPane(frozenTable,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
            frozenScrollPane.border = BorderFactory.createMatteBorder(1, 1, 1, 0, Color.LIGHT_GRAY)

            def mainScrollPane = new JScrollPane(dataTable)
            mainScrollPane.border = BorderFactory.createMatteBorder(1, 0, 1, 1, Color.LIGHT_GRAY)

            // Share the vertical scroll model so both tables scroll together
            mainScrollPane.verticalScrollBar.model = frozenScrollPane.verticalScrollBar.model

            // Row sorter (supports both sorting and filtering)
            def sorter = new TableRowSorter<>(sharedModel)
            frozenTable.rowSorter = sorter
            dataTable.rowSorter   = sorter

            // Filter panel
            def filterPanel    = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4))
            def columnCombo    = new JComboBox<>(headers as Object[])
            def modeCombo      = new JComboBox<>(["Include", "Exclude"] as Object[])
            def filterField    = new JTextField(20)
            def applyButton     = new JButton("Apply")
            def clearButton     = new JButton("Clear")
            def resetSortButton = new JButton("Reset Sort")

            applyButton.addActionListener { event ->
                def text = filterField.text?.trim()
                if (!text) {
                    sorter.rowFilter = null
                    return
                }
                int modelCol = columnCombo.selectedIndex + 1  // +1 because model col 0 is "#"
                boolean isInclude = modeCombo.selectedItem == "Include"
                def regexFilter = RowFilter.regexFilter("(?i)\\Q${text}\\E", modelCol)
                sorter.rowFilter = isInclude ? regexFilter : new RowFilter() {
                    @Override boolean include(RowFilter.Entry entry) { !regexFilter.include(entry) }
                }
            }

            clearButton.addActionListener { event ->
                filterField.text = ""
                sorter.rowFilter = null
            }

            resetSortButton.addActionListener { event ->
                sorter.setSortKeys(null)
            }

            filterPanel.add(new JLabel("Column:"))
            filterPanel.add(columnCombo)
            filterPanel.add(modeCombo)
            filterPanel.add(filterField)
            filterPanel.add(applyButton)
            filterPanel.add(clearButton)
            filterPanel.add(resetSortButton)

            // Column header click → select/deselect that column
            def selectColumn = { JTable t, MouseEvent e ->
                int viewCol = t.tableHeader.columnAtPoint(e.point)
                if (viewCol >= 0) {
                    int modelCol = t.convertColumnIndexToModel(viewCol)
                    selectedModelCol[0] = (selectedModelCol[0] == modelCol) ? -1 : modelCol
                }
                frozenTable.repaint()
                dataTable.repaint()
            }
            frozenTable.tableHeader.addMouseListener(new MouseAdapter() {
                @Override void mouseClicked(MouseEvent e) { selectColumn(frozenTable, e) }
            })
            dataTable.tableHeader.addMouseListener(new MouseAdapter() {
                @Override void mouseClicked(MouseEvent e) { selectColumn(dataTable, e) }
            })

            def tablePanel = new JPanel(new BorderLayout())
            tablePanel.add(frozenScrollPane, BorderLayout.WEST)
            tablePanel.add(mainScrollPane, BorderLayout.CENTER)

            def contentPanel = new JPanel(new BorderLayout())
            contentPanel.add(filterPanel, BorderLayout.NORTH)
            contentPanel.add(tablePanel, BorderLayout.CENTER)

            // Add padding
            ((JPanel) frame.contentPane).border = new EmptyBorder(10, 10, 10, 10)

            frame.add(contentPanel)
            frame.setVisible(true)
          } catch (Exception e) {
            e.printStackTrace()
            JOptionPane.showMessageDialog(null,
                "Error building table view:\n\n${e.class.simpleName}: ${e.message}",
                "View Error", JOptionPane.ERROR_MESSAGE)
          }
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
            def widths  = fwReader.getColumnWidths()

            TableView tableViewer = new TableView(headers, rows, widths)
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
