package ru.otus.lessonplan.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@AllArgsConstructor
@Data
public class LessonPlanItem {
    private String stageName;
    private LocalTime bgnTime;
    private LocalTime timeOffset;
}
