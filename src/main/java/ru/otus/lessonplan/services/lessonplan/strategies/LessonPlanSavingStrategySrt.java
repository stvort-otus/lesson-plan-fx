package ru.otus.lessonplan.services.lessonplan.strategies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.lessonplan.model.LessonPlan;
import ru.otus.lessonplan.model.LessonPlanItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class LessonPlanSavingStrategySrt implements LessonPlanSavingStrategy {
    @Override
    public SavingFormat getSupportedFormat() {
        return SavingFormat.SRT;
    }

    @Override
    public void save(LessonPlan plan, File file) {
        log.info("Saving lesson plan to srt file: {}", file);

        var formatter = DateTimeFormatter.ofPattern("HH:mm:ss,SSS");
        var sb = new StringBuilder();
        for (int i = 0; i < plan.getItemsCount(); i++) {
            var item = plan.getItem(i);
            var timeOffset = item.getTimeOffset();
            if (timeOffset == null) {
                timeOffset = LocalTime.parse("00:00:00");
            }
            timeOffset = timeOffset.withNano(0);
            sb.append(i + 1).append("\n")
                    .append(timeOffset.format(formatter))
                    .append(" --> ")
                    .append(timeOffset.plus(5, ChronoUnit.SECONDS).format(formatter))
                    .append("\n")
                    .append(item.getStageName())
                    .append("\n")
                    .append("\n");

        }
        try (var fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            fw.write(sb.toString());
        } catch (IOException e) {
            log.error("Error during saving lesson plan to srt file", e);
        }

    }
}
