package ru.otus.lessonplan.model;

import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;

public class LessonPlanItemDto {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final SimpleStringProperty stageName;
    private final SimpleStringProperty bgnTime;
    private final SimpleStringProperty timeOffset;

    public LessonPlanItemDto(String stageName, String bgnTime, String timeOffset) {
        this.stageName = new SimpleStringProperty(stageName);
        this.bgnTime = new SimpleStringProperty(bgnTime);
        this.timeOffset = new SimpleStringProperty(timeOffset);
    }

    public LessonPlanItemDto(LessonPlanItem item) {
        this.stageName = new SimpleStringProperty(item.getStageName());
        this.bgnTime = new SimpleStringProperty(item.getBgnTime() == null? "": item.getBgnTime().format(formatter));
        this.timeOffset = new SimpleStringProperty(item.getTimeOffset() == null? "": item.getTimeOffset().format(formatter));
    }

    public String getStageName() {
        return stageName.get();
    }

    public SimpleStringProperty stageNameProperty() {
        return stageName;
    }

    public String getBgnTime() {
        return bgnTime.get();
    }

    public SimpleStringProperty bgnTimeProperty() {
        return bgnTime;
    }

    public String getTimeOffset() {
        return timeOffset.get();
    }

    public SimpleStringProperty timeOffsetProperty() {
        return timeOffset;
    }
}
