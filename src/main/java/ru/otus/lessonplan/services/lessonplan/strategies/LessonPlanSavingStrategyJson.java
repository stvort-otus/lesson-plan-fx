package ru.otus.lessonplan.services.lessonplan.strategies;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.lessonplan.model.LessonPlan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LessonPlanSavingStrategyJson implements LessonPlanSavingStrategy {
    private final Gson gson;

    public LessonPlanSavingStrategyJson() {
        gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (t, type, ctx) -> new JsonPrimitive(t.format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (e, type, ctx) -> LocalTime.parse(e.getAsString()))
                .create();
    }

    @Override
    public SavingFormat getSupportedFormat() {
        return SavingFormat.JSON;
    }

    @Override
    public void save(LessonPlan plan, File file) {
        log.info("Saving lesson plan to json file: {}", file);
        try (var fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(plan, LessonPlan.class, fw);
        } catch (IOException e) {
            log.error("Error during saving lesson plan to json file", e);
        }

    }
}
