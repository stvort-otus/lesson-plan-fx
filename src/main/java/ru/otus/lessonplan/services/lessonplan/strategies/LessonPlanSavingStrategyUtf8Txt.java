package ru.otus.lessonplan.services.lessonplan.strategies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.lessonplan.model.LessonPlan;
import ru.otus.lessonplan.services.LessonPlanTransformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class LessonPlanSavingStrategyUtf8Txt implements LessonPlanSavingStrategy {

    private final LessonPlanTransformer transformer;

    @Override
    public SavingFormat getSupportedFormat() {
        return SavingFormat.UTF8_TXT;
    }

    @Override
    public void save(LessonPlan plan, File file) {
        log.info("Saving lesson plan to txt file: {}", file);

        var lessonPlanAsString = transformer.transform(plan);
        try (var fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            fw.write(lessonPlanAsString);
        } catch (IOException e) {
            log.error("Error during saving lesson plan to txt file", e);
        }
    }
}
