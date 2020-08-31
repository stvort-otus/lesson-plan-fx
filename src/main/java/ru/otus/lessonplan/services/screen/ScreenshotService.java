package ru.otus.lessonplan.services.screen;

import ru.otus.lessonplan.model.ScreenAreaToCapture;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface ScreenshotService {
    void setScreenAreaToCapture(ScreenAreaToCapture area);
    ScreenAreaToCapture getScreenAreaToCapture();

    void captureScreenArea(OutputStream os);
    void captureScreenArea(Consumer<InputStream> onCapture);
}
