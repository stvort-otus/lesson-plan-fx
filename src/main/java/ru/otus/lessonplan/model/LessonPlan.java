package ru.otus.lessonplan.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

import static java.time.LocalTime.parse;
import static ru.otus.lessonplan.utils.StringUtils.withoutBom;

@AllArgsConstructor
@Data
public class LessonPlan {
    private LocalTime lessonStartTime;
    private List<LessonPlanItem> items;

    public LessonPlan() {
        lessonStartTime = parse("20:00:00");
        items = new ArrayList<>();
    }

    public LessonPlanItem getItem(int index) {
        return items.get(index);
    }

    public int getItemsCount() {
        return items.size();
    }

    public void forEachItem(Consumer<LessonPlanItem> action) {
        items.forEach(action);
    }

    public boolean setStageTimeByName(String stageName) {
        return items.stream()
                .filter(i -> withoutBom(i.getStageName()).equalsIgnoreCase(withoutBom(stageName)))
                .filter(i -> i.getBgnTime() == null)
                .findFirst()
                .map(i -> {
                    i.setBgnTime(LocalTime.now());
                    return true;
                }).orElse(false);
    }

    public void setStageTimeByName(String stageName, LocalTime time) {
        items.stream()
                .filter(i -> withoutBom(i.getStageName()).equalsIgnoreCase(withoutBom(stageName)))
                .filter(i -> i.getBgnTime() == null)
                .forEach(i -> i.setBgnTime(time));
    }

    public boolean itemExists(String stageName) {
        return items.stream()
                .anyMatch(i -> withoutBom(i.getStageName()).equalsIgnoreCase(withoutBom(stageName)));
    }

    public void addItem(String stageName){
        items.add(new LessonPlanItem(withoutBom(stageName), null, null));
    }

    public void deleteItem(int index){
        if (index < 0 || index >= items.size()) {
            return;
        }
        items.remove(index);
    }

    public Optional<String> getNextStageFor(String stageName) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getStageName().equalsIgnoreCase(stageName)) {
                return Optional.ofNullable((i < items.size() - 1)? items.get(i + 1).getStageName(): null);
            }
        }
        return Optional.empty();
    }

    public Optional<String> getLastStageName() {
        return items.stream().filter(i -> i.getBgnTime() == null)
                .findFirst()
                .map(LessonPlanItem::getStageName);
    }

    public void setLessonStartTime(LocalTime lessonStartTime) {
        this.lessonStartTime = lessonStartTime;
    }

    public void sortItemsByBgnTime(){
        items.sort(Comparator.comparingInt(o -> o.getBgnTime().toSecondOfDay()));
    }

    public void recalcLabels(){
        items.forEach(i -> {
            if (i.getBgnTime() != null) {
                if (i.getBgnTime().isBefore(lessonStartTime)) {
                    int difLeft = 86400 - lessonStartTime.toSecondOfDay();
                    int difRight = i.getBgnTime().toSecondOfDay();
                    i.setTimeOffset(LocalTime.ofSecondOfDay(difLeft + difRight));
                } else {
                    i.setTimeOffset(i.getBgnTime().minus(lessonStartTime.toSecondOfDay(), ChronoUnit.SECONDS));
                }
            }
        });
    }

    public void setLessonStartTimeAndRecalcLabels(LocalTime lessonStartTime) {
        setLessonStartTime(lessonStartTime);
        recalcLabels();
    }

    public boolean allStagesHasTime(){
        return items.stream().allMatch(i -> i.getBgnTime() != null);
    }

    public void clear(boolean onlyTimes) {
        if (onlyTimes) {
            items.forEach(i -> {
                i.setBgnTime(null);
                i.setTimeOffset(null);
            });
        } else {
            items.clear();
        }
    }

}
