package ru.otus.lessonplan.services.video;


import ru.otus.lessonplan.services.video.callbacks.QRCodeFoundCallback;
import ru.otus.lessonplan.services.video.callbacks.VideoFileProgressCallback;

public interface VideoFileQRCodeExtractor {
    void processVideoFile(String fileName, QRCodeFoundCallback onQRCodeFound, VideoFileProgressCallback onVideoFileProgress);
    void processVideoFile(String fileName, QRCodeFoundCallback onQRCodeFound);
}
