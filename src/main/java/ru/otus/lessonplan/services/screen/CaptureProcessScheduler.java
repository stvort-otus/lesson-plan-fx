package ru.otus.lessonplan.services.screen;

import ru.otus.lessonplan.model.ScreenAreaToCapture;

public interface CaptureProcessScheduler {
    void startCaptureProcess();
    boolean isCaptureProcessRunning();

    void setScreenAreaToCapture(ScreenAreaToCapture area);
    void setOnCapture(Runnable onCapture);

    void shutdownCaptureProcess();
}
