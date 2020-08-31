package ru.otus.lessonplan.fx;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Consumer;

import static ru.otus.lessonplan.utils.FilesUtils.getFileFullPathFromJarFolder;
import static ru.otus.lessonplan.utils.PropertiesHelper.*;

@Component
public class ExtendedFileChooser {

    private static final String LAST_DIRS_PROPERTIES_FILE_PATH = "lastDirs.properties";

    private final FileChooser fileChooser;

    public ExtendedFileChooser() {
        fileChooser = new FileChooser();
    }

    public void showModal(Stage owner, boolean isSaveDialog, String initialFileName, String ext, String dialogKeyForRestoreLastDir, Consumer<File> onSuccess){
        fileChooser.setInitialFileName(initialFileName);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext, ext));
        restoreLastDir(dialogKeyForRestoreLastDir);
        var file = isSaveDialog ? fileChooser.showSaveDialog(owner) : fileChooser.showOpenDialog(owner);
        if (file != null) {
            onSuccess.accept(file);
            storeLastDir(dialogKeyForRestoreLastDir, file);
        }
    }

    private void restoreLastDir(String dialogKeyForRestoreLastDir) {
        var fileName = getFileFullPathFromJarFolder(LAST_DIRS_PROPERTIES_FILE_PATH);
        var lastDialogDirFile = new File(getPropertyFromFile(fileName, dialogKeyForRestoreLastDir));
        if (lastDialogDirFile.exists()) {
            fileChooser.setInitialDirectory(lastDialogDirFile);
        }
    }

    private void storeLastDir(String dialogKeyForRestoreLastDir, File currentFile) {
        if (currentFile == null || !currentFile.getParentFile().exists()) {
            return;
        }
        var fileName = getFileFullPathFromJarFolder(LAST_DIRS_PROPERTIES_FILE_PATH);
        updatePropertyFromFile(fileName, dialogKeyForRestoreLastDir, currentFile.getParentFile().getAbsolutePath());
    }


}
