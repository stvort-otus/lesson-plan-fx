package ru.otus.lessonplan.services.lessonplan.strategies;

import ru.otus.lessonplan.model.LessonPlan;

import java.io.File;

public interface LessonPlanSavingStrategy {
    SavingFormat getSupportedFormat();
    void save(LessonPlan plan, File file);
}
