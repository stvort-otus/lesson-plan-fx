package ru.otus.lessonplan.services.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.otus.lessonplan.model.ScreenAreaToCapture;
import ru.otus.lessonplan.services.QRCodeService;
import ru.otus.lessonplan.services.lessonplan.LessonPlanHolder;
import ru.otus.lessonplan.services.lessonplan.strategies.SavingFormat;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.otus.lessonplan.services.lessonplan.LessonPlanHolder.CURRENT_EXECUTION_JSON_FILE;

@RequiredArgsConstructor
@Service
public class CaptureProcessSchedulerImpl implements CaptureProcessScheduler {
    private static final int CORE_POOL_SIZE = 1;
    private static final int CAPTURE_PERIOD = 700;

    private final ScreenshotService screenshotService;
    private final QRCodeService qrCodeService;
    private final LessonPlanHolder lessonPlanHolder;

    @Getter
    private boolean captureProcessRunning = false;

    private ScheduledExecutorService captureProcess = null;

    @Setter
    private Runnable onCapture = null;

    @Override
    public void startCaptureProcess() {
        captureProcess = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
        captureProcess.scheduleAtFixedRate(this::capture, 0, CAPTURE_PERIOD, TimeUnit.MILLISECONDS);
        captureProcessRunning = true;
    }

    @Override
    public void setScreenAreaToCapture(ScreenAreaToCapture area) {
        screenshotService.setScreenAreaToCapture(area);
    }


    private void capture() {
        try {
            screenshotService.captureScreenArea((is) -> {
                var stageName = qrCodeService.readBarCode(is);
                if (!"".equals(stageName) && lessonPlanHolder.setStageTimeByName(stageName)) {
                    if (onCapture != null) {
                        onCapture.run();
                    }
                    lessonPlanHolder.saveLessonPlan(new File(CURRENT_EXECUTION_JSON_FILE), SavingFormat.JSON);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownCaptureProcess() {
        if (!captureProcessRunning) {
            return;
        }
        captureProcessRunning = false;
        captureProcess.shutdown();
        try {
            captureProcess.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        captureProcess.shutdownNow();
    }

}
