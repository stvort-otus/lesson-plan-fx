package ru.otus.lessonplan.services.video;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.springframework.stereotype.Service;

@Service
public class FFmpegFrameGrabberFactory {

    public FFmpegFrameGrabber createFrameGrabber(String fileName) {
        var frameGrabber = new FFmpegFrameGrabber(fileName);
        frameGrabber.setImageMode(FrameGrabber.ImageMode.GRAY);
        frameGrabber.setOption("preset", "ultrafast");
        return frameGrabber;
    }
}
