package ru.otus.lessonplan.fx;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.function.Consumer;

import static ru.otus.lessonplan.utils.FilesUtils.getFileFullPathFromJarFolder;
import static ru.otus.lessonplan.utils.PropertiesHelper.getPropertyFromFile;
import static ru.otus.lessonplan.utils.PropertiesHelper.updatePropertyFromFile;

@Component
public class ExtendedDirectoryChooser {

    private static final String LAST_DIRS_PROPERTIES_FILE_PATH = "lastDirs.properties";

    private final DirectoryChooser directoryChooser;

    public ExtendedDirectoryChooser() {
        directoryChooser = new DirectoryChooser();
    }

    public void showModal(Stage owner, Consumer<File> onSuccess){
        showModal(owner, null, onSuccess);
    }

    public void showModal(Stage owner, String dialogKeyForRestoreLastDir, Consumer<File> onSuccess){
        restoreLastDir(dialogKeyForRestoreLastDir);
        var file = directoryChooser.showDialog(owner);
        if (file != null) {
            onSuccess.accept(file);
            storeLastDir(dialogKeyForRestoreLastDir, file);
        }
    }

    private void restoreLastDir(String dialogKeyForRestoreLastDir) {
        var fileName = getFileFullPathFromJarFolder(LAST_DIRS_PROPERTIES_FILE_PATH);
        var lastDialogDirFile = new File(getPropertyFromFile(fileName, dialogKeyForRestoreLastDir));
        if (lastDialogDirFile.exists()) {
            directoryChooser.setInitialDirectory(lastDialogDirFile);
        } else if (lastDialogDirFile.getParentFile() != null && lastDialogDirFile.getParentFile().exists()) {
            directoryChooser.setInitialDirectory(lastDialogDirFile.getParentFile());
        }
    }

    private void storeLastDir(String dialogKeyForRestoreLastDir, File currentFile) {
        if (currentFile == null || !currentFile.getParentFile().exists()) {
            return;
        }
        var fileName = getFileFullPathFromJarFolder(LAST_DIRS_PROPERTIES_FILE_PATH);
        updatePropertyFromFile(fileName, dialogKeyForRestoreLastDir, currentFile.getAbsolutePath());
    }


}
