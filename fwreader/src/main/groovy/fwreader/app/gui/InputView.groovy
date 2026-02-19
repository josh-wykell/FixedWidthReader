package fwreader.app.gui

import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.concurrent.CountDownLatch
import javax.swing.*

class InputView {
    final File manifestFile = new File(System.getProperty("user.home"), ".fwreader/fwFileManifest.ser")
    final File configsDir   = new File(System.getProperty("user.home"), ".fwreader/configs")

    Map<String, File> loadFileManifest() {
        if (!manifestFile.exists()) return [:]

        try {
            def objectInputStream = new ObjectInputStream(new FileInputStream(manifestFile))
            def map = objectInputStream.readObject()
            objectInputStream.close()
            return map
        } catch (Exception e) {
            return [:]
        }
    }

    void saveManifest(Map<String, File> configs) {
        manifestFile.parentFile.mkdirs()
        def objectOutputStream = new ObjectOutputStream(new FileOutputStream(manifestFile))
        objectOutputStream.writeObject(configs)
        objectOutputStream.close()
    }

    private File copyConfigToStore(File source, String name) {
        configsDir.mkdirs()
        def ext  = source.name.contains('.') ? source.name.substring(source.name.lastIndexOf('.')) : ''
        def dest = new File(configsDir, name + ext)
        dest.bytes = source.bytes
        return dest
    }

    // Step 1: always shown — pick the fixed-width data file
    private void showFWFileChooser(Map selectedFiles, CountDownLatch latch) {
        def frame = new JFrame("Select Fixed Width File")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(600, 120)
        frame.layout = new GridLayout(2, 2)

        def fwFileLabel     = new JLabel("Fixed Width File:")
        def fwFileTextField = new JTextField(40)
        def chooseButton    = new JButton("Browse...")
        def nextButton      = new JButton("Next")

        chooseButton.addActionListener { event ->
            def fileChooser = new JFileChooser()
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                fwFileTextField.text    = fileChooser.selectedFile.absolutePath
                selectedFiles["fwFile"] = fileChooser.selectedFile
            }
        }

        nextButton.addActionListener { event ->
            def fw = new File(fwFileTextField.text)
            if (fw.exists()) {
                selectedFiles["fwFile"] = fw
                frame.dispose()
                showConfigSelection(selectedFiles, latch)
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a valid file.")
            }
        }

        frame.add(fwFileLabel)
        frame.add(fwFileTextField)
        frame.add(chooseButton)
        frame.add(nextButton)
        frame.setVisible(true)
    }

    // Step 2: branch on whether any configs are saved
    private void showConfigSelection(Map selectedFiles, CountDownLatch latch) {
        Map<String, File> configManifest = loadFileManifest()
        if (configManifest.isEmpty()) {
            showConfigChooser(configManifest, selectedFiles, latch)
        } else {
            showSavedConfigsDialog(configManifest, selectedFiles, latch)
        }
    }

    // Step 2a: saved configs exist — show the list by user-given name
    private void showSavedConfigsDialog(Map<String, File> configManifest, Map selectedFiles, CountDownLatch latch) {
        def frame = new JFrame("Select Config")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 300)
        frame.layout = new BorderLayout()

        def list = new JList(configManifest.keySet() as Object[])
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.selectedIndex = 0

        def buttonPanel = new JPanel()
        def openButton  = new JButton("Open")
        def newButton   = new JButton("New Config")

        openButton.addActionListener { event ->
            def idx = list.selectedIndex
            if (idx >= 0) {
                def name = configManifest.keySet().toList()[idx]
                selectedFiles["configFile"] = configManifest[name]
                latch.countDown()
                frame.dispose()
            }
        }

        newButton.addActionListener { event ->
            frame.dispose()
            showConfigChooser(configManifest, selectedFiles, latch)
        }

        buttonPanel.add(openButton)
        buttonPanel.add(newButton)

        frame.add(new JScrollPane(list), BorderLayout.CENTER)
        frame.add(buttonPanel, BorderLayout.SOUTH)
        frame.setVisible(true)
    }

    // Step 2b: no saved configs, or user chose "New Config" — pick, name, and save
    private void showConfigChooser(Map<String, File> configManifest, Map selectedFiles, CountDownLatch latch) {
        def frame = new JFrame("Add Config")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(600, 150)
        frame.layout = new GridLayout(3, 2)

        def configFileLabel     = new JLabel("YAML Config File:")
        def configFileTextField = new JTextField(40)
        def configNameLabel     = new JLabel("Config Name:")
        def configNameTextField = new JTextField(40)
        def chooseButton        = new JButton("Browse...")
        def doneButton          = new JButton("Done")

        chooseButton.addActionListener { event ->
            def fileChooser = new JFileChooser()
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                configFileTextField.text    = fileChooser.selectedFile.absolutePath
                selectedFiles["configFile"] = fileChooser.selectedFile
            }
        }

        doneButton.addActionListener { event ->
            def config = new File(configFileTextField.text)
            def name   = configNameTextField.text?.trim()

            if (!config.exists()) {
                JOptionPane.showMessageDialog(frame, "Please select a valid config file.")
                return
            }
            if (!name) {
                JOptionPane.showMessageDialog(frame, "Please enter a name for this config.")
                return
            }
            if (configManifest.containsKey(name)) {
                JOptionPane.showMessageDialog(frame, "A config named \"${name}\" already exists. Please choose a different name.")
                return
            }

            def storedConfig = copyConfigToStore(config, name)
            selectedFiles["configFile"] = storedConfig
            configManifest[name] = storedConfig
            saveManifest(configManifest)
            latch.countDown()
            frame.dispose()
        }

        frame.add(configFileLabel)
        frame.add(configFileTextField)
        frame.add(configNameLabel)
        frame.add(configNameTextField)
        frame.add(chooseButton)
        frame.add(doneButton)
        frame.setVisible(true)
    }

    Map<String, File> selectFiles() {
        Map selectedFiles = [:]
        def latch         = new CountDownLatch(1)

        SwingUtilities.invokeLater {
            showFWFileChooser(selectedFiles, latch)
        }

        try {
            latch.await()
        } catch (InterruptedException e) {
            e.printStackTrace()
        }

        return selectedFiles
    }
}
