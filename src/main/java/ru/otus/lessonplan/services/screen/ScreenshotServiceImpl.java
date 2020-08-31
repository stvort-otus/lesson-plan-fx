package ru.otus.lessonplan.services.screen;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.otus.lessonplan.model.ScreenAreaToCapture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Consumer;

@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    private static final String LAST_AREA_TO_CAPTURE_PROPS_FILE = "./lastAreaToCaptureProps.properties";
    private static final String CAPTURE_AREA_X = "capture.area.x";
    private static final String CAPTURE_AREA_Y = "capture.area.y";
    private static final String CAPTURE_AREA_W = "capture.area.w";
    private static final String CAPTURE_AREA_H = "capture.area.h";

    static {
        System.setProperty("java.awt.headless", "false");
    }

    private final Robot robot;
    private final Rectangle areaToCapture;

    public ScreenshotServiceImpl() {
        try {
            robot = new Robot();
            areaToCapture = restoreAreaToCaptureFromFile();
        } catch (Exception e) {
            throw new ScreenshotServiceException(e);
        }
    }

    private Rectangle restoreAreaToCaptureFromFile() throws IOException {
        var lastAreaToCaptureProps = Paths.get(LAST_AREA_TO_CAPTURE_PROPS_FILE);
        if (Files.exists(lastAreaToCaptureProps)) {
            var properties = new Properties();
            try(var is = Files.newInputStream(lastAreaToCaptureProps)) {
                properties.load(is);
            }

            return new Rectangle(Integer.parseInt(properties.getProperty(CAPTURE_AREA_X, "0")),
                    Integer.parseInt(properties.getProperty(CAPTURE_AREA_Y, "0")),
                    Integer.parseInt(properties.getProperty(CAPTURE_AREA_W, "300")),
                    Integer.parseInt(properties.getProperty(CAPTURE_AREA_H, "300")));
        }
        return new Rectangle(0, 0, 300, 300);
    }

    private void saveAreaToCaptureToFile() {
        var lastAreaToCaptureProps = Paths.get(LAST_AREA_TO_CAPTURE_PROPS_FILE);
        var properties = new Properties();
        properties.setProperty(CAPTURE_AREA_X, String.valueOf(areaToCapture.x));
        properties.setProperty(CAPTURE_AREA_Y, String.valueOf(areaToCapture.y));
        properties.setProperty(CAPTURE_AREA_W, String.valueOf(areaToCapture.width));
        properties.setProperty(CAPTURE_AREA_H, String.valueOf(areaToCapture.height));
        try (var os = Files.newOutputStream(lastAreaToCaptureProps)){
            properties.store(os, "");
        } catch (Exception e) {
            throw new ScreenshotServiceException(e);
        }


    }

    @Override
    public synchronized void setScreenAreaToCapture(ScreenAreaToCapture area) {
        areaToCapture.x = area.getX();
        areaToCapture.y = area.getY();
        areaToCapture.width = area.getWidth();
        areaToCapture.height = area.getHeight();
        saveAreaToCaptureToFile();
    }

    @Override
    public synchronized ScreenAreaToCapture getScreenAreaToCapture() {
        return new ScreenAreaToCapture(areaToCapture.x, areaToCapture.y, areaToCapture.width, areaToCapture.height);
    }


    @Override
    @SneakyThrows
    public synchronized void captureScreenArea(OutputStream os) {
        var image = robot.createScreenCapture(areaToCapture);
        ImageIO.write(image, "bmp", os);
    }

    @Override
    public synchronized void captureScreenArea(Consumer<InputStream> onCapture) {
        try (var bos = new ByteArrayOutputStream()) {
            captureScreenArea(bos);
            try (var is = new ByteArrayInputStream(bos.toByteArray())) {
                onCapture.accept(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
