package ru.otus.lessonplan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lessonplan.video-file-qr-code-extractor")
public class VideoFileQRCodeExtractorProps {
    private int grabStepFrames;
    private int degreeOfParallelism;
    private boolean ignoreQrCodeTextWithNumbersOnly;
}
