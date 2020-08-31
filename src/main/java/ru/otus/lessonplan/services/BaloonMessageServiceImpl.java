package ru.otus.lessonplan.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.lessonplan.services.lessonplan.LessonPlanHolder;

@RequiredArgsConstructor
@Service
public class BaloonMessageServiceImpl implements BaloonMessageService {

    private static final int MAX_BALOON_MSG_LEN = 127;

    private final LessonPlanHolder lessonPlanHolder;

    @Override
    public String getCurrentPlanPositionMessage() {
        var sb = new StringBuilder();

        var lastStage = lessonPlanHolder.getLastStageName().orElse("");
        if (!"".equals(lastStage)) {
            sb.append(lastStage).append("\n");
            lastStage = lessonPlanHolder.getNextStageFor(lastStage).orElse("");
            if (!"".equals(lastStage) && sb.toString().length() + lastStage.length() < MAX_BALOON_MSG_LEN) {
                sb.append(lastStage).append("\n");
                lastStage = lessonPlanHolder.getNextStageFor(lastStage).orElse("");
                if (!"".equals(lastStage) && sb.toString().length() + lastStage.length() < MAX_BALOON_MSG_LEN) {
                    sb.append(lastStage).append("\n");
                }
            }
        }

        return sb.toString();
    }
}
