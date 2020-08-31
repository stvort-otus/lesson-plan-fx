package ru.otus.lessonplan.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class CaptureAreaController {

    @FXML
    private ImageView applyBtn;

    private double xOffset;
    private double yOffset;

    @FXML
    public void onMousePressed(MouseEvent evt) {
        xOffset = evt.getSceneX();
        yOffset = evt.getSceneY();
    }

    @FXML
    public void onMouseDragged(MouseEvent evt) {
        Node source = (Node) evt.getSource();
        Window stage = source.getScene().getWindow();
        stage.setX(evt.getScreenX() - xOffset);
        stage.setY(evt.getScreenY() - yOffset);
    }

    @FXML
    public void onImageViewClick(MouseEvent evt) {
        Node source = (Node) evt.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("apply-btn.png")) {
            if (is != null) {
                Image image = new Image(is);
                applyBtn.setImage(image);
            }
        }
    }
}