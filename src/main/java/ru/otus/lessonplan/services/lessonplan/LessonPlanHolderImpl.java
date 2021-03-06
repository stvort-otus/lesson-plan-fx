package ru.otus.lessonplan.services.lessonplan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.lessonplan.model.LessonPlan;
import ru.otus.lessonplan.model.LessonPlanItem;
import ru.otus.lessonplan.services.QRCodeService;
import ru.otus.lessonplan.services.lessonplan.strategies.LessonPlanLoadingStrategy;
import ru.otus.lessonplan.services.lessonplan.strategies.LessonPlanSavingStrategy;
import ru.otus.lessonplan.services.lessonplan.strategies.SavingFormat;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.otus.lessonplan.utils.FilesUtils.correctFileName;

@Slf4j
@Service
public class LessonPlanHolderImpl implements LessonPlanHolder {

    private final Map<SavingFormat, LessonPlanSavingStrategy> savingStrategyMap;
    private final Map<SavingFormat, LessonPlanLoadingStrategy> loadingStrategyMap;
    private final LessonPlan lessonPlan;
    private final QRCodeService qrCodeService;

    public LessonPlanHolderImpl(List<LessonPlanSavingStrategy> savingStrategies,
                                List<LessonPlanLoadingStrategy> loadingStrategies,
                                QRCodeService qrCodeService) {
        lessonPlan = new LessonPlan();

        savingStrategyMap = savingStrategies.stream()
                .collect(Collectors.toMap(LessonPlanSavingStrategy::getSupportedFormat, s -> s));

        loadingStrategyMap = loadingStrategies.stream()
                .collect(Collectors.toMap(LessonPlanLoadingStrategy::getSupportedFormat, s -> s));

        this.qrCodeService = qrCodeService;

    }

    @Override
    public void loadLessonPlan(File file, SavingFormat format) {
        synchronized (lessonPlan) {
            lessonPlan.getItems().clear();
            Optional.ofNullable(loadingStrategyMap.get(format))
                    .flatMap(s -> s.load(file))
                    .ifPresent(p -> {
                        lessonPlan.setLessonStartTime(p.getLessonStartTime());
                        p.getItems().forEach(i -> lessonPlan.getItems().add(i));
                    });
        }
    }

    @Override
    public void saveLessonPlan(File file, SavingFormat format) {
        Optional.ofNullable(savingStrategyMap.get(format))
                .ifPresent(s -> s.save(lessonPlan, file));
    }

    @Override
    public void addStageIfNotExists(String stageName) {
        synchronized (lessonPlan) {
            if (!lessonPlan.itemExists(stageName)) {
                lessonPlan.addItem(stageName);
            }
        }
    }

    @Override
    public void deleteItem(int index) {
        synchronized (lessonPlan) {
            lessonPlan.deleteItem(index);
        }
    }

    @Override
    public void sortItemsByBgnTime() {
        synchronized (lessonPlan) {
            lessonPlan.sortItemsByBgnTime();
        }
    }


    @Override
    public void clear(boolean onlyTimes) {
        synchronized (lessonPlan) {
            lessonPlan.clear(onlyTimes);
        }
    }

    @Override
    public LocalTime getLessonStartTime() {
        return lessonPlan.getLessonStartTime();
    }

    public void forEachLessonPlanItem(Consumer<LessonPlanItem> action) {
        lessonPlan.forEachItem(action);
    }

    @Override
    public boolean setStageTimeByName(String stageName) {
        synchronized(lessonPlan) {
            return lessonPlan.setStageTimeByName(stageName);
        }
    }

    @Override
    public void setStageTimeByName(String stageName, LocalTime time) {
        synchronized(lessonPlan) {
            lessonPlan.setStageTimeByName(stageName, time);
        }
    }

    @Override
    public Optional<String> getLastStageName() {
        return lessonPlan.getLastStageName();
    }

    @Override
    public Optional<String> getNextStageFor(String stageName) {
        return lessonPlan.getNextStageFor(stageName);
    }

    @Override
    public void generateQRCodesByPlan(File targetDir, int size) {
        try {
            for (int i = 0; i < lessonPlan.getItemsCount(); i++) {
                var item = lessonPlan.getItems().get(i);
                var fileName = correctFileName(String.format("%03d - %s%s", i + 1, item.getStageName(), ".png"));
                var path = Paths.get(targetDir.getAbsolutePath(), fileName);
                qrCodeService.generateQRCodeImageFile(item.getStageName(), size, size, path.toAbsolutePath().toString());
            }
        } catch (Exception e) {
            log.error("Error during generation of qr-codes files", e);
        }
    }

    @Override
    public void setLessonStartTime(LocalTime lessonStartTime) {
        synchronized (lessonPlan) {
            lessonPlan.setLessonStartTime(lessonStartTime);
        }
    }

    @Override
    public void recalcLabels() {
        synchronized (lessonPlan) {
            lessonPlan.recalcLabels();
        }
    }

    @Override
    public void setLessonStartTimeAndRecalcLabels(LocalTime lessonStartTime) {
        synchronized (lessonPlan) {
            lessonPlan.setLessonStartTimeAndRecalcLabels(lessonStartTime);
        }
    }

    @Override
    public boolean allStagesHasTime() {
        return lessonPlan.allStagesHasTime();
    }
}
