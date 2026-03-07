package fwreader.app.gui

import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.concurrent.CountDownLatch
import javax.swing.*
import javax.swing.border.EmptyBorder
import org.yaml.snakeyaml.Yaml

class InputView {
    final File manifestFile = new File(System.getProperty("user.home"), ".fwreader/fwFileManifest.ser")
    final File configsDir   = new File(System.getProperty("user.home"), ".fwreader/configs")
    final File lastDirFile  = new File(System.getProperty("user.home"), ".fwreader/lastDir.txt")

    private File loadLastDir() {
        if (lastDirFile.exists()) {
            def dir = new File(lastDirFile.text.trim())
            if (dir.isDirectory()) return dir
        }
        return null
    }

    private void saveLastDir(File dir) {
        lastDirFile.parentFile.mkdirs()
        lastDirFile.text = dir.absolutePath
    }

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

    private boolean isValidYamlConfig(File file) {
        try {
            def yaml = new Yaml()
            def content = yaml.load(file.text)
            return content != null && content instanceof Map
        } catch (Exception e) {
            return false
        }
    }

    // Step 1: always shown — pick the fixed-width data file
    private void showFWFileChooser(Map selectedFiles, CountDownLatch latch) {
        def frame = new JFrame("Select Fixed Width File")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = new BorderLayout(10, 10)
        ((JPanel) frame.contentPane).border = new EmptyBorder(10, 10, 10, 10)

        def fwFileLabel     = new JLabel("Fixed Width File:")
        def fwFileTextField = new JTextField(40)
        def chooseButton    = new JButton("Browse...")
        def nextButton      = new JButton("Next")

        // Pre-populate if returning from a later step
        if (selectedFiles["fwFile"]) {
            fwFileTextField.text = selectedFiles["fwFile"].absolutePath
        }

        chooseButton.addActionListener { event ->
            def fileChooser = new JFileChooser(loadLastDir() ?: new File(System.getProperty("user.home")))
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                fwFileTextField.text    = fileChooser.selectedFile.absolutePath
                selectedFiles["fwFile"] = fileChooser.selectedFile
                saveLastDir(fileChooser.selectedFile.parentFile)
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

        // Top panel: browse button
        def topPanel = new JPanel()
        topPanel.add(chooseButton)

        // Center panel: label + text field
        def centerPanel = new JPanel(new BorderLayout(5, 0))
        centerPanel.add(fwFileLabel, BorderLayout.WEST)
        centerPanel.add(fwFileTextField, BorderLayout.CENTER)

        // Bottom panel: next button
        def bottomPanel = new JPanel()
        bottomPanel.add(nextButton)

        frame.add(topPanel, BorderLayout.NORTH)
        frame.add(centerPanel, BorderLayout.CENTER)
        frame.add(bottomPanel, BorderLayout.SOUTH)
        frame.pack()
        frame.setLocationRelativeTo(null)
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
        ((JPanel) frame.contentPane).border = new EmptyBorder(10, 10, 10, 10)

        def list = new JList(configManifest.keySet() as Object[])
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.selectedIndex = 0

        def buttonPanel  = new JPanel()
        def openButton   = new JButton("Open")
        def deleteButton = new JButton("Delete")
        def newButton    = new JButton("New Config")
        def backButton   = new JButton("Back")

        openButton.addActionListener { event ->
            def idx = list.selectedIndex
            if (idx >= 0) {
                def name = configManifest.keySet().toList()[idx]
                selectedFiles["configFile"] = configManifest[name]
                latch.countDown()
                frame.dispose()
            }
        }

        deleteButton.addActionListener { event ->
            def idx = list.selectedIndex
            if (idx >= 0) {
                def name = configManifest.keySet().toList()[idx]
                def fileToDelete = configManifest[name]

                def confirm = JOptionPane.showConfirmDialog(frame,
                    "Delete config \"${name}\"?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION)

                if (confirm == JOptionPane.YES_OPTION) {
                    configManifest.remove(name)
                    saveManifest(configManifest)
                    fileToDelete.delete()

                    frame.dispose()
                    showConfigSelection(selectedFiles, latch)
                }
            }
        }

        newButton.addActionListener { event ->
            frame.dispose()
            showConfigChooser(configManifest, selectedFiles, latch)
        }

        backButton.addActionListener { event ->
            frame.dispose()
            showFWFileChooser(selectedFiles, latch)
        }

        buttonPanel.add(openButton)
        buttonPanel.add(deleteButton)
        buttonPanel.add(newButton)
        buttonPanel.add(backButton)

        frame.add(new JScrollPane(list), BorderLayout.CENTER)
        frame.add(buttonPanel, BorderLayout.SOUTH)
        frame.setVisible(true)
    }

    // Step 2b: no saved configs, or user chose "New Config" — pick, name, and save
    private void showConfigChooser(Map<String, File> configManifest, Map selectedFiles, CountDownLatch latch) {
        def frame = new JFrame("Add Config")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = new BorderLayout(10, 10)
        ((JPanel) frame.contentPane).border = new EmptyBorder(10, 10, 10, 10)

        def configFileLabel     = new JLabel("YAML Config File:")
        def configFileTextField = new JTextField(40)
        def configNameLabel     = new JLabel("Config Name:")
        def configNameTextField = new JTextField(40)
        def chooseButton        = new JButton("Browse...")
        def doneButton          = new JButton("Done")
        def backButton          = new JButton("Back")

        chooseButton.addActionListener { event ->
            def fileChooser = new JFileChooser(loadLastDir() ?: new File(System.getProperty("user.home")))
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                configFileTextField.text    = fileChooser.selectedFile.absolutePath
                selectedFiles["configFile"] = fileChooser.selectedFile
                saveLastDir(fileChooser.selectedFile.parentFile)
            }
        }

        doneButton.addActionListener { event ->
            def config = new File(configFileTextField.text)
            def name   = configNameTextField.text?.trim()

            if (!config.exists()) {
                def retry = JOptionPane.showConfirmDialog(frame,
                    "Please select a valid config file.\n\nWould you like to try again?",
                    "Invalid File",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)
                if (retry != JOptionPane.YES_OPTION) {
                    frame.dispose()
                    showFWFileChooser(selectedFiles, latch)
                }
                return
            }
            if (!config.canRead()) {
                def retry = JOptionPane.showConfirmDialog(frame,
                    "The selected file cannot be read. Please check file permissions.\n\nWould you like to try again?",
                    "Cannot Read File",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)
                if (retry != JOptionPane.YES_OPTION) {
                    frame.dispose()
                    showFWFileChooser(selectedFiles, latch)
                }
                return
            }
            if (!isValidYamlConfig(config)) {
                def retry = JOptionPane.showConfirmDialog(frame,
                    "The selected file is not a valid YAML file.\n\nWould you like to try again?",
                    "Invalid YAML",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)
                if (retry != JOptionPane.YES_OPTION) {
                    frame.dispose()
                    showFWFileChooser(selectedFiles, latch)
                }
                return
            }
            if (!name) {
                def retry = JOptionPane.showConfirmDialog(frame,
                    "Please enter a name for this config.\n\nWould you like to try again?",
                    "Missing Name",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)
                if (retry != JOptionPane.YES_OPTION) {
                    frame.dispose()
                    showFWFileChooser(selectedFiles, latch)
                }
                return
            }
            if (configManifest.containsKey(name)) {
                def retry = JOptionPane.showConfirmDialog(frame,
                    "A config named \"${name}\" already exists. Please choose a different name.\n\nWould you like to try again?",
                    "Duplicate Name",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)
                if (retry != JOptionPane.YES_OPTION) {
                    frame.dispose()
                    showFWFileChooser(selectedFiles, latch)
                }
                return
            }

            def storedConfig = copyConfigToStore(config, name)
            selectedFiles["configFile"] = storedConfig
            configManifest[name] = storedConfig
            saveManifest(configManifest)
            latch.countDown()
            frame.dispose()
        }

        backButton.addActionListener { event ->
            frame.dispose()
            showFWFileChooser(selectedFiles, latch)
        }

        // Top panel: browse button
        def topPanel = new JPanel()
        topPanel.add(chooseButton)

        // Center panel: two rows (file path + name)
        def centerPanel = new JPanel(new GridLayout(2, 1, 5, 5))

        def fileRow = new JPanel(new BorderLayout(5, 0))
        fileRow.add(configFileLabel, BorderLayout.WEST)
        fileRow.add(configFileTextField, BorderLayout.CENTER)

        def nameRow = new JPanel(new BorderLayout(5, 0))
        nameRow.add(configNameLabel, BorderLayout.WEST)
        nameRow.add(configNameTextField, BorderLayout.CENTER)

        centerPanel.add(fileRow)
        centerPanel.add(nameRow)

        // Bottom panel: back and done buttons
        def bottomPanel = new JPanel()
        bottomPanel.add(backButton)
        bottomPanel.add(doneButton)

        frame.add(topPanel, BorderLayout.NORTH)
        frame.add(centerPanel, BorderLayout.CENTER)
        frame.add(bottomPanel, BorderLayout.SOUTH)
        frame.pack()
        frame.setLocationRelativeTo(null)
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
