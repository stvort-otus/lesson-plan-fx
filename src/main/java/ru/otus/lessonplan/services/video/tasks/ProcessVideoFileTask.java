package ru.otus.lessonplan.services.video.tasks;

import javafx.concurrent.Task;
import lombok.Builder;
import ru.otus.lessonplan.services.lessonplan.LessonPlanHolder;
import ru.otus.lessonplan.services.video.VideoFileQRCodeExtractor;

import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

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
        try {
            videoFileQRCodeExtractor.processVideoFile(videoFile.getAbsolutePath(), entry -> {

                        var time = lessonTime.plus(entry.getTime().toSecondOfDay(), ChronoUnit.SECONDS);

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
                    },
                    (totalFrames, currentFrame, percent) -> {
                        var canFinish = !addStageIfNotExist && lessonPlanHolder.allStagesHasTime();
                        var progress = canFinish? 100: percent;
                        updateProgress(progress, 100);
                        updateMessage(String.format("%d%%", progress));
                        return canFinish;
                    }
            );
        } catch (Exception e) {
            if (onException != null) {
                onException.accept(e);
            }
        }
        return null;
    }
}
