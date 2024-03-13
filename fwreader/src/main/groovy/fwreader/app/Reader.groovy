package fwreader.app

import groovy.transform.CompileStatic
import java.nio.charset.StandardCharsets
import org.yaml.snakeyaml.Yaml

@CompileStatic
class Reader {
    File          fwFile
    File          fwConfigFile
    LinkedHashMap fwConfigMap

    Reader(File fwFile, File fwConfig) {
        def yamlConfig = new Yaml()

        this.fwFile       = fwFile
        this.fwConfigFile = fwConfig
        this.fwConfigMap  = yamlConfig.load(fwConfigFile.text)
    }

    List<String> getHeaders() {
        List<String> headers = fwConfigMap.keySet() as List<String>
      //  return headers
    }



    List<List> collectRows() {
        List<List> rows       = []
        List<Integer> ranges  = fwConfigMap.values() as List<Integer>
        BufferedReader reader = new BufferedReader(new java.io.FileReader(fwFile, StandardCharsets.UTF_8))

        try {
            String line

            while (line = reader.readLine()) {
                Integer startIndex = 0
                List<String> row   = []

                ranges.collect {range ->
                    String field = line.substring(startIndex, Math.min(startIndex + range, line.length()))?.trim()
                    startIndex += range

                    //If the field is empty create a place holder empty string
                    if (!field) field = ''
                    row << field
                }

                rows << row
            }
        } finally {
            reader.close()
        }

        return rows
    }
}
