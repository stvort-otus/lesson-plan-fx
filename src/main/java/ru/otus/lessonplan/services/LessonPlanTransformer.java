package ru.otus.lessonplan.services;

import org.springframework.stereotype.Component;
import ru.otus.lessonplan.model.LessonPlan;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class LessonPlanTransformer {

    public String transform(LessonPlan plan) {
        var formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        var sb = new StringBuilder();
        for (int i = 0; i < plan.getItemsCount(); i++) {
            var item = plan.getItem(i);
            var timeOffset = item.getTimeOffset();
            if (timeOffset == null) {
                timeOffset = LocalTime.parse("00:00:00");
            }
            sb.append(timeOffset.format(formatter))
                    .append(" ")
                    .append(item.getStageName())
                    .append("\n");

        }
        return sb.toString();
    }
}
