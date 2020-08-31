package ru.otus.lessonplan.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public final class JavaFxUtils {

    private JavaFxUtils() {
    }

     public static void executeConfirmationDialog(String title, String text, Runnable onYes, Runnable onCancel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(text);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.YES)).setText("Да");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Нет");
        Optional<ButtonType> buttonType = alert.showAndWait();
        buttonType.map(bt -> {
            if (bt == ButtonType.CANCEL) {
                onCancel.run();
            } else {
                onYes.run();
            }
            return false;
        }).orElseGet(() -> {
            onCancel.run();
            return false;
        });
    }

    public static void executeWarningAlert(String caption, String text) {
        executeAlert(caption, text, Alert.AlertType.WARNING);
    }

    public static void executeAlert(String caption, String text, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(caption);
        alert.setHeaderText(text);
        alert.showAndWait();
    }

    public static Stage createModalUtilityStageAndWait(String fxml,
                                                       Stage primaryStage,
                                                       int x, int y,
                                                       int width, int height,
                                                       double opacity) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(JavaFxUtils.class.getClassLoader().getResource(fxml));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        Stage stage = new Stage();

        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(scene);
        stage.setOpacity(opacity);

        stage.initModality(Modality.WINDOW_MODAL);

        stage.initOwner(primaryStage);
        scene.getWindow().setX(x);
        scene.getWindow().setY(y);
        scene.getWindow().setWidth(width);
        scene.getWindow().setHeight(height);
        stage.showAndWait();

        return stage;
    }

}
