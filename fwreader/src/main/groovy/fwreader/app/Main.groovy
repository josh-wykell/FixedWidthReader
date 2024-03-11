package fwreader.app

static void main(String[] args) {
    println 'running reader'
    File fwFile = new File(args[0])
    File fwConfig = new File(args[1])

    Reader fwReader = new Reader(fwFile, fwConfig)
    fwReader.read()
}

