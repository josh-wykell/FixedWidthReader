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

            Reader fwReader = new Reader(fwFile, fwConfig)
            def headers = fwReader.getHeaders()
            def rows    = fwReader.collectRows()

            TableView tableViewer = new TableView(headers, rows)
            tableViewer.createView(fwFile.name)
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

