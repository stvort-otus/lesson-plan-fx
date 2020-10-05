package ru.otus.lessonplan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import ru.otus.lessonplan.controllers.MainViewController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;

@SpringBootApplication
public class LessonplanApplication extends Application {

    private static final String TRAY_ICON_FILE_NAME = "appicon-tray.png";
    private static final String TRAY_ICON_TOOL_TIP = "План занятия";
    private static final String BALOON_TITLE = "Далее:";
    private static final int MAIN_FORM_WIDTH = 600;
    private static final int MAIN_FORM_HEIGHT = 550;
    private static final String APP_ICON_FILE_NAME = "appicon.png";
    private static final String MAIN_FORM_TITLE = "План занятия";
    private static final String MAIN_VIEW_FXML = "/fxml/main_view.fxml";
    
    private ConfigurableApplicationContext springContext;
    private FXMLLoader fxmlLoader;
    private SystemTray tray = null;
    private TrayIcon trayIcon = null;

    @Override
    public void init() {
        var rawParam = getParameters().getRaw().toArray(new String[0]);
        springContext = SpringApplication.run(LessonplanApplication.class, rawParam);
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(springContext::getBean);
    }

    @Override
    public void stop() {
        springContext.stop();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        fxmlLoader.setLocation(getClass().getResource(MAIN_VIEW_FXML));
        Parent rootNode = fxmlLoader.load();


        primaryStage.setTitle(MAIN_FORM_TITLE);
        InputStream iconStream = getClass().getClassLoader().getResourceAsStream(APP_ICON_FILE_NAME);
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }

        MainViewController controller = fxmlLoader.getController();
        primaryStage.setOnCloseRequest(evt -> {
            controller.exitApplication(evt);
            if (!evt.isConsumed() && tray != null && trayIcon != null) {
                tray.remove(trayIcon);
            }
        });

        Scene scene = new Scene(rootNode, MAIN_FORM_WIDTH, MAIN_FORM_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
        javax.swing.SwingUtilities.invokeLater(() -> addAppToTray(controller));
    }

    private void addAppToTray(MainViewController controller) {
        try {
            Toolkit.getDefaultToolkit();

            if (!SystemTray.isSupported()) {
                return;
            }

            tray = java.awt.SystemTray.getSystemTray();
            URL appIcon = getClass().getClassLoader().getResource(TRAY_ICON_FILE_NAME);
            if (appIcon != null) {
                java.awt.Image image = ImageIO.read(appIcon);
                trayIcon = new java.awt.TrayIcon(image);
                trayIcon.setToolTip(TRAY_ICON_TOOL_TIP);
                trayIcon.addActionListener(event -> Platform.runLater(() -> {
                    String toolTip = controller.getCurrentPlanPositionMessage();
                    if (!"".equals(toolTip)) {
                        trayIcon.displayMessage(BALOON_TITLE, toolTip, TrayIcon.MessageType.NONE);
                    }
                }));
                tray.add(trayIcon);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}