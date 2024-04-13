package fwreader.app

import fwreader.app.views.*

static void main(String[] args) {
    InputView input = new InputView()
    def files = input.selectFiles()
    File fwFile   = files["fwFile"]//new File(args[0])
    File fwConfig = files["configFile"]//new File(args[1])

    Reader fwReader = new Reader(fwFile, fwConfig)
    def headers = fwReader.getHeaders()
    def rows      = fwReader.collectRows()

    TableView tableViewer = new TableView(headers, rows)
    tableViewer.createView(fwFile.name)
}

