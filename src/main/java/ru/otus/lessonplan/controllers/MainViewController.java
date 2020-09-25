package ru.otus.lessonplan.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;

import java.io.IOException;

public interface MainViewController {

    void loadPlanOnAction(ActionEvent evt);
    void generateQRCodesOnAction(ActionEvent evt);

    void loadLastLessonExecutionMIOnAction(ActionEvent evt);
    void loadLessonExecutionMIOnAction(ActionEvent evt);
    void loadLessonExecutionFromVideoFileMIOnAction(ActionEvent evt);

    void saveLessonExecutionMIOnAction(ActionEvent evt);
    void saveLessonExecutionAsSrtMIOnAction(ActionEvent evt);
    void saveLessonExecutionForYoutubeMIOnAction(ActionEvent evt);

    @FXML
    void saveLessonExecutionForYoutubeWithBomMIOnAction(ActionEvent evt);

    void setCurrentCaptureAreaMIOnAction(ActionEvent evt) throws IOException;
    void deleteSelectedItemsMIOnAction(ActionEvent evt);

    void captureBtnOnAction(ActionEvent evt);
    void recalcBtnOnAction(ActionEvent evt);
    void recalcFromVideoBtnOnAction(ActionEvent evt);

    void exitApplication(WindowEvent evt);

    String getCurrentPlanPositionMessage();
}
