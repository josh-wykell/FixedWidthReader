package fwreader.app.views

import javax.swing.*
import java.awt.GridLayout
import java.util.concurrent.CountDownLatch
//import javax.swing.filechooser.FileNameExtensionFilter
class InputView {


    Map<String, File> selectFiles() {
        Map selectedFiles = [:]
        def latch = new CountDownLatch(3)

        SwingUtilities.invokeLater {
            def frame = new JFrame("File Chooser Example")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(800, 200)
            frame.layout = new GridLayout(3, 2)

            def fwFileLabel = new JLabel("Fixed Width File:")
            def fwFileTextField = new JTextField(200)
            def fwFileButton = new JButton("Choose Fixed Width File")
            fwFileButton.addActionListener { event ->
                def fileChooser = new JFileChooser()
                def result = fileChooser.showOpenDialog(frame)
                if (result == JFileChooser.APPROVE_OPTION) {
                    File fwFile = fileChooser.selectedFile
                    fwFileTextField.text = fwFile.absolutePath
                    selectedFiles["fwFile"] = fwFile
                    latch.countDown()
                }
            }

            def configFileLabel = new JLabel("Yaml Configuration File:")
            def configFileTextField = new JTextField(20)
            def configFileButton = new JButton("Choose Yaml Config File")
            configFileButton.addActionListener { event ->
                def fileChooser = new JFileChooser()
                def result = fileChooser.showOpenDialog(frame)
                if (result == JFileChooser.APPROVE_OPTION) {
                    File configFile = fileChooser.selectedFile
                    configFileTextField.text = configFile.absolutePath
                    selectedFiles["configFile"] = configFile
                    latch.countDown()
                }
            }

            def processButton = new JButton("Done")
            processButton.addActionListener { event ->
                def fw = new File(fwFileTextField.text)
                def config = new File(configFileTextField.text)
                if (fw.exists() && config.exists()) {
                    println "File 1: ${fw.absolutePath}"
                    println "File 2: ${config.absolutePath}"
                    // Process the files here
                } else {
                    println "One or both files do not exist."
                }
                latch.countDown()
                frame.dispose()
            }

            frame.add(fwFileLabel)
            frame.add(fwFileTextField)
            frame.add(fwFileButton)
            frame.add(configFileLabel)
            frame.add(configFileTextField)
            frame.add(configFileButton)
            frame.add(processButton)

            frame.setVisible(true)
        }

        try {
            latch.await()
        } catch (InterruptedException e) {
            e.printStackTrace()
        }

        return selectedFiles
    }

// Call the method to create and show the GUI
//    def selectedFiles = createAndShowGUI()
//    println "Selected File 1: ${selectedFiles.first?.absolutePath}"
//    println "Selected File 2: ${selectedFiles.second?.absolutePath}"
}
