package fwreader.app

import fwreader.app.gui.*

static void main(String[] args) {
    InputView input = new InputView()
    def files = input.selectFiles()
    File fwFile   = files["fwFile"]
    File fwConfig = files["configFile"]

    Reader fwReader = new Reader(fwFile, fwConfig)
    def headers = fwReader.getHeaders()
    def rows      = fwReader.collectRows()

    TableView tableViewer = new TableView(headers, rows)
    tableViewer.createView(fwFile.name)
}

