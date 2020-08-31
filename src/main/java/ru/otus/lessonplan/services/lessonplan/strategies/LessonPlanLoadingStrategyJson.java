package ru.otus.lessonplan.services.lessonplan.strategies;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.stereotype.Component;
import ru.otus.lessonplan.model.LessonPlan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
public class LessonPlanLoadingStrategyJson implements LessonPlanLoadingStrategy {
    private final Gson gson;

    public LessonPlanLoadingStrategyJson() {
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
    public Optional<LessonPlan> load(File file) {
        log.info("Loading lesson plan from json file: {}", file);
        if (!file.exists()) {
            log.info("File does not exists {}", file);
            return Optional.empty();
        }

        try {
            log.info("File charset detection");
            var charset = UniversalDetector.detectCharset(file);
            log.info("Detected file charset: {}", charset);

            if (!charset.equalsIgnoreCase("utf-8") && !charset.equalsIgnoreCase("windows-1251")) {
                log.error("Unsupported charset: {}", charset);
                throw new UnsupportedCharsetException(charset);
            }

            log.info("Convert file to lesson plan object");
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset)) {
                return Optional.ofNullable(gson.fromJson(reader, LessonPlan.class));
            }
        } catch (IOException e) {
            log.error("Error during loading lesson plan from json", e);
        }
        return Optional.empty();
    }
}
