package ru.otus.lessonplan.services.lessonplan.strategies;

import ru.otus.lessonplan.model.LessonPlan;

import java.io.File;
import java.util.Optional;

public interface LessonPlanLoadingStrategy {
    SavingFormat getSupportedFormat();
    Optional<LessonPlan> load(File file);
}
