package ru.otus.lessonplan.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.scene.control.LocalTimeTextField;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import ru.otus.lessonplan.fx.ExtendedDirectoryChooser;
import ru.otus.lessonplan.fx.ExtendedFileChooser;
import ru.otus.lessonplan.model.LessonPlanItemDto;
import ru.otus.lessonplan.model.ScreenAreaToCapture;
import ru.otus.lessonplan.services.BaloonMessageService;
import ru.otus.lessonplan.services.lessonplan.LessonPlanHolder;
import ru.otus.lessonplan.services.lessonplan.strategies.SavingFormat;
import ru.otus.lessonplan.services.screen.CaptureProcessScheduler;
import ru.otus.lessonplan.services.screen.ScreenshotService;
import ru.otus.lessonplan.services.video.VideoFileQRCodeExtractor;
import ru.otus.lessonplan.services.video.tasks.ProcessVideoFileTask;
import ru.otus.lessonplan.utils.FilesUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED;
import static ru.otus.lessonplan.services.lessonplan.LessonPlanHolder.CURRENT_EXECUTION_JSON_FILE;
import static ru.otus.lessonplan.utils.JavaFxUtils.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MainViewControllerImpl implements MainViewController {

    private static final String LOAD_PLAN_DIALOG_KEY = "load-plan-dialog";
    private static final String SELECT_VIDEO_DIALOG_KEY = "select-video-dialog";
    private static final String QR_CODES_GENERATE_DIALOG_KEY = "qr-codes-generate-dialog";
    private static final String LOAD_LESSON_EXECUTION_DIALOG_KEY = "load-lesson-execution-dialog";
    private static final String SAVE_LESSON_EXECUTION_DIALOG_KEY = "save-lesson-execution-dialog";
    private static final String SAVE_AS_SRT_DIALOG_KEY = "save-as-srt-dialog";
    private static final String SAVE_FOR_YOUTUBE_DIALOG_KEY = "save-for-youtube-dialog";

    private static final String LESSON_EXECUTION_DIALOG_INITIAL_FILE_NAME = "Занятие";
    private static final String SAVE_AS_STR_DIALOG_INITIAL_FILE_NAME = "Субтитры к записи занятия";
    private static final String SAVE_FOR_YOUTUBE_DIALOG_INITIAL_FILE_NAME = "Временные метки к записи занятия";

    private static final String TXT_EXT = "*.txt";
    private static final String JSON_EXT = "*.json";
    private static final String SRT_EXT = "*.srt";
    private static final String MP4_EXT = "*.mp4";

    private static final String STAGE_NAME_COLUMN_NAME = "stageName";
    private static final String BGN_TIME_COLUMN_NAME = "bgnTime";
    private static final String TIME_OFFSET_COLUMN_NAME = "timeOffset";

    private static final String TIME_FMT = "HH:mm:ss";
    private static final String CAPTURE_AREA_SELECTION_FXML_FILE = "fxml/capture_area_selection.fxml";
    private static final double CAPTURE_AREA_SELECTION_OPACITY = 0.5d;

    private static final String WARNING_CAPTION = "Предупреждение";
    private static final String BAD_TIME_FMT_MSG = "Не верный формат времени!";
    private static final String BAD_FILE_MSG = "Не удалось обратиться к файлу!";
    private static final String VIDEO_PROCESSING_ERROR_MSG = "Ошибка в процессе обработки видео!";
    private static final String CONFIRM_CAPTION = "Подтверждение";
    private static final String REALLY_EXIT_MSG = "Вы действительно хотите выйти?";
    private static final String WRONG_FILE_CHARSET_MSG = "Кодировка файла не поддерживается";

    private static final String CAPTURE_BUTTON_CAPTION_STOP = "Остановить";
    private static final String CAPTURE_BUTTON_CAPTION_START = "Начать захват";


    private final ObservableList<LessonPlanItemDto> plan =
            FXCollections.observableArrayList();

    @FXML private VBox rootVBox;
    @FXML private TableView<LessonPlanItemDto> lessonExecutionTV;
    @FXML private TableColumn<LessonPlanItemDto, String> stageNameColumn;
    @FXML private TableColumn<LessonPlanItemDto, String> bgnTimeColumn;
    @FXML private TableColumn<LessonPlanItemDto, String> timeOffsetColumn;
    @FXML private Button captureBtn;
    @FXML private Button recalcBtn;
    @FXML private Button recalcFromVideoBtn;
    @FXML private Label lessonStartTimeLab;
    @FXML private LocalTimeTextField lessonStartTimeLTF;

    @FXML private MenuBar mainMenu;

    @FXML private AnchorPane mainPane;

    @FXML private MenuItem generateQRCodesMI;
    @FXML private MenuItem saveLessonExecutionMI;
    @FXML private MenuItem saveLessonExecutionAsSrtMI;
    @FXML private MenuItem saveLessonExecutionForYoutubeMI;

    @FXML private MenuItem deleteSelectedItemsMI;


    @Value("${lessonplan.qr-code-size: 100}")
    private int qrCodeSize;

    private final ExtendedFileChooser fileChooser;
    private final ExtendedDirectoryChooser directoryChooser;

    private final ScreenshotService screenshotService;
    private final LessonPlanHolder lessonPlanHolder;

    private final CaptureProcessScheduler captureProcessScheduler;
    private final VideoFileQRCodeExtractor videoFileQRCodeExtractor;

    private final BaloonMessageService baloonMessageService;

    @FXML
    private void initialize() {
        log.info("Start controller initialization");
        captureProcessScheduler.setOnCapture(this::refreshLessonPlanTable);

        stageNameColumn.setCellValueFactory(new PropertyValueFactory<>(STAGE_NAME_COLUMN_NAME));
        bgnTimeColumn.setCellValueFactory(new PropertyValueFactory<>(BGN_TIME_COLUMN_NAME));
        timeOffsetColumn.setCellValueFactory(new PropertyValueFactory<>(TIME_OFFSET_COLUMN_NAME));
        lessonExecutionTV.setItems(plan);

        //lessonStartTimeLTF.setDisable(false);

        lessonStartTimeLTF.setDateTimeFormatter(DateTimeFormatter.ofPattern(TIME_FMT));
        lessonStartTimeLTF.setLocalTime(LocalTime.parse("20:00"));

        lessonStartTimeLTF.setParseErrorCallback(c -> {
            log.info("Bad lesson start time inputted", c);
            executeWarningAlert(WARNING_CAPTION, BAD_TIME_FMT_MSG);
            return null;
        });

        lessonExecutionTV.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        log.info("Controller initialization finished");
    }

    @Override
    public void exitApplication(WindowEvent evt) {
        log.info("Exit application");
        if (mainPane.isDisabled()) {
            log.info("Application busy. Exit is canceled");
            evt.consume();
            return;
        }
        log.info("Showing exit-confirmation dialog");
        executeConfirmationDialog(CONFIRM_CAPTION, REALLY_EXIT_MSG,
                captureProcessScheduler::shutdownCaptureProcess, evt::consume);
    }

    @Override
    public String getCurrentPlanPositionMessage() {
        return baloonMessageService.getCurrentPlanPositionMessage();
    }

    private Stage createModalCaptureAreaStageAndWait(ScreenAreaToCapture area) throws IOException {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        return createModalUtilityStageAndWait(CAPTURE_AREA_SELECTION_FXML_FILE,
                primaryStage, area.getX(), area.getY(), area.getWidth(), area.getHeight(),
                CAPTURE_AREA_SELECTION_OPACITY);
    }

    private void refreshLessonPlanTable() {
        synchronized (plan) {
            plan.clear();
            lessonPlanHolder.forEachLessonPlanItem(i -> plan.add(new LessonPlanItemDto(i)));
            enableControls(plan.size() > 0);
        }
    }

    private void enableControls(boolean enabled) {
        captureBtn.setDisable(!enabled);
        recalcBtn.setDisable(!enabled);
        recalcFromVideoBtn.setDisable(!enabled);
        lessonStartTimeLab.setDisable(!enabled);
        lessonStartTimeLTF.setDisable(!enabled);
        saveLessonExecutionMI.setDisable(!enabled);
        saveLessonExecutionAsSrtMI.setDisable(!enabled);
        saveLessonExecutionForYoutubeMI.setDisable(!enabled);
        generateQRCodesMI.setDisable(!enabled);
        deleteSelectedItemsMI.setDisable(!enabled);
    }

    private void enableControlPanes(boolean enabled) {
        mainMenu.setDisable(!enabled);
        mainPane.setDisable(!enabled);
    }


    private void loadLessonExecutionFromJsonFile(File file) {
        lessonPlanHolder.loadLessonPlan(file, SavingFormat.JSON);
        lessonStartTimeLTF.setLocalTime(lessonPlanHolder.getLessonStartTime());
        refreshLessonPlanTable();
    }

    private void saveCurrentLessonExecution() {
        try {
            var ceFile = Paths.get(FilesUtils.getJarFolder(), CURRENT_EXECUTION_JSON_FILE);
            lessonPlanHolder.saveLessonPlan(ceFile.toFile(), SavingFormat.JSON);
        } catch (Exception e) {
            executeWarningAlert(WARNING_CAPTION, BAD_FILE_MSG);
        }
    }


    @Override
    @FXML
    public void loadPlanOnAction(ActionEvent evt) {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        fileChooser.showModal(primaryStage, false, "",
                TXT_EXT, LOAD_PLAN_DIALOG_KEY, file -> {
                    try {
                        lessonPlanHolder.loadLessonPlan(file, SavingFormat.TXT);
                        lessonStartTimeLTF.setLocalTime(lessonPlanHolder.getLessonStartTime());
                    } catch (UnsupportedCharsetException e) {
                        executeWarningAlert(WARNING_CAPTION, WRONG_FILE_CHARSET_MSG + String.format(" (%s)", e.getCharsetName()));
                    } catch (Exception e) {
                        executeWarningAlert(WARNING_CAPTION, BAD_FILE_MSG);
                    }
                    refreshLessonPlanTable();
                });
    }

    @FXML
    @Override
    public void loadLessonExecutionFromVideoFileMIOnAction(ActionEvent evt) {
        loadLessonExecutionFromVideoFile(false);
    }

    @Override
    @FXML
    public void loadLastLessonExecutionMIOnAction(ActionEvent evt) {
        try {
            var ceFile = Paths.get(FilesUtils.getJarFolder(), CURRENT_EXECUTION_JSON_FILE);
            loadLessonExecutionFromJsonFile(ceFile.toFile());
        } catch (UnsupportedCharsetException e) {
            executeWarningAlert(WARNING_CAPTION, WRONG_FILE_CHARSET_MSG + String.format(" (%s)", e.getCharsetName()));
        } catch (Exception e) {
            executeWarningAlert(WARNING_CAPTION, BAD_FILE_MSG);
        }
    }

    @Override
    @FXML
    public void loadLessonExecutionMIOnAction(ActionEvent evt) {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        fileChooser.showModal(primaryStage, false, "", JSON_EXT, LOAD_LESSON_EXECUTION_DIALOG_KEY,
                file -> {
                    try {
                        loadLessonExecutionFromJsonFile(file);
                    } catch (UnsupportedCharsetException e) {
                        executeWarningAlert(WARNING_CAPTION,
                                WRONG_FILE_CHARSET_MSG + String.format(" (%s)", e.getCharsetName()));
                    } catch (Exception e) {
                        executeWarningAlert(WARNING_CAPTION, BAD_FILE_MSG);
                    }
                });
    }

    @Override
    @FXML
    public void saveLessonExecutionMIOnAction(ActionEvent evt) {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        fileChooser.showModal(primaryStage, true, LESSON_EXECUTION_DIALOG_INITIAL_FILE_NAME,
                JSON_EXT, SAVE_LESSON_EXECUTION_DIALOG_KEY, f -> lessonPlanHolder.saveLessonPlan(f, SavingFormat.JSON));
    }

    @Override
    @FXML
    public void saveLessonExecutionAsSrtMIOnAction(ActionEvent evt) {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        fileChooser.showModal(primaryStage, true, SAVE_AS_STR_DIALOG_INITIAL_FILE_NAME,
                SRT_EXT, SAVE_AS_SRT_DIALOG_KEY, f -> lessonPlanHolder.saveLessonPlan(f, SavingFormat.SRT));
    }

    @Override
    @FXML
    public void saveLessonExecutionForYoutubeMIOnAction(ActionEvent evt) {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        fileChooser.showModal(primaryStage, true, SAVE_FOR_YOUTUBE_DIALOG_INITIAL_FILE_NAME,
                TXT_EXT, SAVE_FOR_YOUTUBE_DIALOG_KEY, f -> lessonPlanHolder.saveLessonPlan(f, SavingFormat.TXT));
    }

    @Override
    @FXML
    public void setCurrentCaptureAreaMIOnAction(ActionEvent evt) throws IOException {
        var captureAreaStage = createModalCaptureAreaStageAndWait(screenshotService.getScreenAreaToCapture());
        captureProcessScheduler.setScreenAreaToCapture(new ScreenAreaToCapture(
                captureAreaStage.getScene().getWindow().getX(),
                captureAreaStage.getScene().getWindow().getY(),
                captureAreaStage.getScene().getWindow().getWidth(),
                captureAreaStage.getScene().getWindow().getHeight()));
    }

    @Override
    @FXML
    public void deleteSelectedItemsMIOnAction(ActionEvent evt) {
        var selectedIndices = lessonExecutionTV.getSelectionModel().getSelectedIndices();
        for (int i = selectedIndices.size() - 1; i >= 0 ; i--) {
            lessonPlanHolder.deleteItem(selectedIndices.get(i));
        }
        saveCurrentLessonExecution();
        refreshLessonPlanTable();
    }


    @Override
    @SneakyThrows
    @FXML
    public void captureBtnOnAction(ActionEvent evt) {
        if (!captureProcessScheduler.isCaptureProcessRunning()) {
            captureProcessScheduler.startCaptureProcess();

            captureBtn.setText(CAPTURE_BUTTON_CAPTION_STOP);
        } else {
            captureBtn.setText(CAPTURE_BUTTON_CAPTION_START);
            captureProcessScheduler.shutdownCaptureProcess();
        }
    }

    @Override
    public void recalcBtnOnAction(ActionEvent evt) {
        try {
            var lessonTime = lessonStartTimeLTF.getLocalTime();
            lessonPlanHolder.setLessonStartTimeAndRecalcLabels(lessonTime);
            refreshLessonPlanTable();
            saveCurrentLessonExecution();
        } catch (DateTimeParseException e) {
            executeWarningAlert(WARNING_CAPTION, BAD_TIME_FMT_MSG);
        }
    }

    private void clearLessonPlan(LocalTime lessonStartTime, boolean onlyTimes) {
        lessonPlanHolder.clear(onlyTimes);
        lessonPlanHolder.setLessonStartTime(lessonStartTime);
    }

    private AnchorPane createProgressPane(Task<Void> taskToBind) {
        var progressBar = new ProgressBar(0);
        var progressLab = new Label();

        var progressPane = new AnchorPane(progressBar, progressLab);
        progressPane.setId("progressPane");

        progressPane.setMaxHeight(60);
        AnchorPane.setTopAnchor(progressBar, 5d);
        AnchorPane.setBottomAnchor(progressBar, 5d);
        AnchorPane.setLeftAnchor(progressBar, 8d);
        AnchorPane.setRightAnchor(progressBar, 50d);

        AnchorPane.setTopAnchor(progressLab, 5d);
        AnchorPane.setBottomAnchor(progressLab, 5d);
        AnchorPane.setRightAnchor(progressLab, 8d);

        rootVBox.getChildren().add(progressPane);

        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(taskToBind.progressProperty());
        progressLab.textProperty().unbind();
        progressLab.textProperty().bind(taskToBind.messageProperty());


        return progressPane;
    }

    @FXML
    @Override
    public void recalcFromVideoBtnOnAction(ActionEvent evt) {
        loadLessonExecutionFromVideoFile(true);
    }

    @Override
    @FXML
    public void generateQRCodesOnAction(ActionEvent evt) {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        directoryChooser.showModal(primaryStage, QR_CODES_GENERATE_DIALOG_KEY,
                file -> lessonPlanHolder.generateQRCodesByPlan(file, qrCodeSize));
    }

    public void loadLessonExecutionFromVideoFile(boolean planExists) {
        var primaryStage = (Stage) rootVBox.getScene().getWindow();
        var lessonStartTime = lessonStartTimeLTF.getLocalTime();
        var exceptions = new ArrayList<Exception>();
        fileChooser.showModal(primaryStage, false, "", MP4_EXT,
                SELECT_VIDEO_DIALOG_KEY, file -> {

                    enableControlPanes(false);
                    clearLessonPlan(lessonStartTime, planExists);

                    var processVideoFileTask = ProcessVideoFileTask.builder().videoFile(file).lessonTime(lessonStartTime)
                            .videoFileQRCodeExtractor(videoFileQRCodeExtractor)
                            .lessonPlanHolder(lessonPlanHolder)
                            .addStageIfNotExist(!planExists)
                            .onException(exceptions::add)
                            .onLabelsUpdated(this::refreshLessonPlanTable)
                            .build();

                    var progressPane = createProgressPane(processVideoFileTask);
                    processVideoFileTask.addEventHandler(WORKER_STATE_SUCCEEDED, t -> {
                        if (!exceptions.isEmpty()) {
                            rootVBox.setDisable(false);
                            executeWarningAlert(WARNING_CAPTION, VIDEO_PROCESSING_ERROR_MSG);
                        }
                        refreshLessonPlanTable();
                        rootVBox.getChildren().remove(progressPane);
                        enableControlPanes(true);
                        saveCurrentLessonExecution();
                    });
                    new Thread(processVideoFileTask).start();
                });
    }

}