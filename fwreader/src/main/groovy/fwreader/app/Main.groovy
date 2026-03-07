package fwreader.app

import fwreader.app.gui.*
import javax.swing.JOptionPane

static void main(String[] args) {
    // macOS: Use screen menu bar and set app name
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.name", "FixedWidthReader")

    while (true) {
        try {
            InputView input = new InputView()
            def files = input.selectFiles()
            File fwFile   = files["fwFile"]
            File fwConfig = files["configFile"]

            println "[DEBUG] fwFile:   ${fwFile}"
            println "[DEBUG] fwConfig: ${fwConfig}"

            Reader fwReader = new Reader(fwFile, fwConfig)
            println "[DEBUG] Reader created"

            def headers = fwReader.getHeaders()
            println "[DEBUG] Headers: ${headers}"

            def rows = fwReader.collectRows()
            println "[DEBUG] Rows collected: ${rows.size()}"

            def widths = fwReader.getColumnWidths()
            println "[DEBUG] Widths: ${widths}"

            TableView tableViewer = new TableView(headers, rows, widths)
            println "[DEBUG] TableView created, calling createView..."
            tableViewer.createView(fwFile.name)
            println "[DEBUG] createView called (runs async on Swing thread)"
            break // Success - exit loop
        } catch (Exception e) {
            def retry = JOptionPane.showConfirmDialog(null,
                "An error occurred while reading the file:\n\n${e.message}\n\nWould you like to try again?",
                "Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE)

            if (retry != JOptionPane.YES_OPTION) {
                System.exit(0)
            }
        }
    }
}

