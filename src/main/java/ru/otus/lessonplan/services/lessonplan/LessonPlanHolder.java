package ru.otus.lessonplan.services.lessonplan;

import ru.otus.lessonplan.model.LessonPlanItem;
import ru.otus.lessonplan.services.lessonplan.strategies.SavingFormat;

import java.io.File;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Consumer;

public interface LessonPlanHolder {
    String CURRENT_EXECUTION_JSON_FILE = "currentExecution.json";

    void clear(boolean onlyTimes);

    LocalTime getLessonStartTime();

    void forEachLessonPlanItem(Consumer<LessonPlanItem> action);

    void saveLessonPlan(File file, SavingFormat format);
    void loadLessonPlan(File file, SavingFormat format);

    void addStageIfNotExists(String stageName);
    void deleteItem(int index);
    void sortItemsByBgnTime();

    boolean setStageTimeByName(String stageName);
    void setStageTimeByName(String stageName, LocalTime time);
    Optional<String> getLastStageName();
    Optional<String> getNextStageFor(String stageName);

    void generateQRCodesByPlan(File targetDir, int size);
    void setLessonStartTime(LocalTime lessonStartTime);
    void recalcLabels();
    void setLessonStartTimeAndRecalcLabels(LocalTime lessonStartTime);

    boolean allStagesHasTime();

}
