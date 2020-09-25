package ru.otus.lessonplan.services.lessonplan.strategies;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.stereotype.Component;
import ru.otus.lessonplan.model.LessonPlan;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class LessonPlanLoadingStrategyTxt implements LessonPlanLoadingStrategy {

    private static final LocalTime DEFAULT_LESSON_START_TIME = LocalTime.parse("20:00");

    @Override
    public SavingFormat getSupportedFormat() {
        return SavingFormat.UTF8_TXT;
    }

    @Override
    public Optional<LessonPlan> load(File file) {
        log.info("Loading lesson plan from txt file: {}", file);
        if (!file.exists()) {
            log.info("File does not exists {}", file);
            return Optional.empty();
        }

        var path = file.toPath();
        try {

            log.info("File charset detection");
            var charset = UniversalDetector.detectCharset(file);
            log.info("Detected file charset: {}", charset);

            var plan = new LessonPlan();
            List<String> strings;

            if (charset.equalsIgnoreCase("utf-8")) {
                strings = Files.readAllLines(path, StandardCharsets.UTF_8);
            } else if (charset.equalsIgnoreCase("windows-1251")) {
                strings = Files.readAllLines(path, Charset.forName("windows-1251"));
            } else if (charset.equalsIgnoreCase("US-ASCII")) {
                strings = Files.readAllLines(path, StandardCharsets.US_ASCII);
            } else {
                log.error("Unsupported charset: {}", charset);
                throw new UnsupportedCharsetException(charset);
            }

            plan.setLessonStartTime(DEFAULT_LESSON_START_TIME);
            strings.stream()
                    .filter(s -> !"".equalsIgnoreCase(s.trim()))
                    .forEach(plan::addItem);

            return Optional.of(plan);
        } catch (IOException e) {
            log.error("Error during loading lesson plan from txt", e);
        }

        return Optional.empty();
    }
}
