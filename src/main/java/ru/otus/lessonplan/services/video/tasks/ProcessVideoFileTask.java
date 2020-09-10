package ru.otus.lessonplan.services.video.tasks;

import javafx.concurrent.Task;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import ru.otus.lessonplan.services.lessonplan.LessonPlanHolder;
import ru.otus.lessonplan.services.video.VideoFileQRCodeExtractor;

import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

@Slf4j
@Builder
public class ProcessVideoFileTask extends Task<Void> {

    private final VideoFileQRCodeExtractor videoFileQRCodeExtractor;
    private final LessonPlanHolder lessonPlanHolder;

    private final File videoFile;
    private final LocalTime lessonTime;

    private final Consumer<Exception> onException;
    private final Runnable onLabelsUpdated;

    private boolean addStageIfNotExist = false;


    @Override
    protected Void call() throws Exception {
        log.info("Start processing video file: {}", videoFile.getAbsolutePath());
        try {
            videoFileQRCodeExtractor.processVideoFile(videoFile.getAbsolutePath(), entry -> {

                        var time = lessonTime.plus(entry.getTime().toSecondOfDay(), ChronoUnit.SECONDS);
                        synchronized (lessonPlanHolder) {
                            if (addStageIfNotExist) {
                                lessonPlanHolder.addStageIfNotExists(entry.getMessage());
                            }

                            lessonPlanHolder.setStageTimeByName(entry.getMessage(), time);

                            if (addStageIfNotExist) {
                                lessonPlanHolder.sortItemsByBgnTime();
                            }

                            lessonPlanHolder.recalcLabels();

                            if (onLabelsUpdated != null) {
                                onLabelsUpdated.run();
                            }
                        }
                    },
                    (totalFrames, currentFrame, percent) -> {
                        updateProgress(percent, 100);
                        updateMessage(String.format("%d%%", percent));
                        return false;
                    }
            );
        } catch (Exception e) {
            log.error("Error during processing video file", e);
            if (onException != null) {
                onException.accept(e);
            }
        }
        return null;
    }
}
