package ru.otus.lessonplan.services.video.callbacks;

public interface VideoFileProgressCallback {
    boolean apply(long totalFrames, long currentFrame, int percent);
}
