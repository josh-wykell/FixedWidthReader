package fwreader.app

static void main(String[] args) {
    println 'running reader'
    File fwFile   = new File(args[0])
    File fwConfig = new File(args[1])

    Reader fwReader = new Reader(fwFile, fwConfig)
    def headers = fwReader.getHeaders()
    def rows      = fwReader.collectRows()

    FileView viewer = new FileView(headers, rows)
    viewer.createView()
}

