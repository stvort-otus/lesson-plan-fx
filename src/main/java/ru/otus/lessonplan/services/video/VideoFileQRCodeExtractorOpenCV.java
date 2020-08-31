package ru.otus.lessonplan.services.video;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.otus.lessonplan.model.QRCodeEntry;
import ru.otus.lessonplan.services.QRCodeService;
import ru.otus.lessonplan.services.video.callbacks.QRCodeFoundCallback;
import ru.otus.lessonplan.services.video.callbacks.VideoFileProgressCallback;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Slf4j
@Service
public class VideoFileQRCodeExtractorOpenCV implements VideoFileQRCodeExtractor {
    private static final int GRAB_STEP = 48;
    private static final Pattern BAD_QR_CODE_TEXT_PATTERN =
            Pattern.compile("^(\\d|~|@|#|%|\\^|&|\\*|\\(|\\)|-|=|\\+|_|\\$|!|\"|:|;|,|\\.|<|>|\\?|\\|/|№|\\r|\\n)+$");

    private final int degreeOfParallelism;
    private final boolean ignoreQrCodeTextWithNumbersOnly;
    private final FFmpegFrameGrabberFactory grabberFactory;
    private final QRCodeService qrCodeService;

    public VideoFileQRCodeExtractorOpenCV(
            @Value("${lessonplan.degree-of-parallelism:1}") int degreeOfParallelism,
            @Value("${lessonplan.ignore-qrcode-text-with-numbers-only:false}") boolean ignoreQrCodeTextWithNumbersOnly,
            FFmpegFrameGrabberFactory grabberFactory,
            QRCodeService qrCodeService) {

        this.degreeOfParallelism = degreeOfParallelism;
        this.ignoreQrCodeTextWithNumbersOnly = ignoreQrCodeTextWithNumbersOnly;
        this.grabberFactory = grabberFactory;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public void processVideoFile(String fileName, QRCodeFoundCallback onQRCodeFound,
                                 VideoFileProgressCallback onVideoFileProgress) {

        log.info("Start processing video file: {}", fileName);
        var t = System.currentTimeMillis();

        var latch = new CountDownLatch(degreeOfParallelism);
        try {
            var pool = Executors.newFixedThreadPool(degreeOfParallelism);

            HashMap<String, Integer> progressMap = new HashMap<>();
            VideoFileProgressCallback progressCallback = (totalFrames, currentFrame, percent) -> {
                synchronized (progressMap) {
                    log.info("Thread process percent {} = {} ", Thread.currentThread().getName(), percent);
                    progressMap.put(Thread.currentThread().getName(), percent);
                    int sum = progressMap.values().stream().mapToInt(Integer::intValue).sum();
                    if (onVideoFileProgress != null) {
                        return onVideoFileProgress.apply(totalFrames, currentFrame, sum);
                    }
                }
                return false;
            };

            IntStream.range(1, degreeOfParallelism + 1).forEachOrdered(i -> {
                pool.submit(() -> processPartOfVideoFile(grabberFactory.createFrameGrabber(fileName),
                        i, degreeOfParallelism, latch, onQRCodeFound, progressCallback));
            });

            latch.await();

            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
            pool.shutdownNow();
        } catch (Exception e) {
            log.error("Error during processing video file", e);
            throw new VideoFileProcessingException(e);
        }
        log.info("Finished video file processing. Duration: {} ", System.currentTimeMillis() - t);
    }

    @Override
    public void processVideoFile(String fileName, QRCodeFoundCallback onQRCodeFound) {
        processVideoFile(fileName, onQRCodeFound, null);
    }

    private boolean checkQRCodeMessage(String qrCodeMessage) {
        var res = !"".equals(qrCodeMessage);
        if (res && ignoreQrCodeTextWithNumbersOnly) {
            return !BAD_QR_CODE_TEXT_PATTERN.matcher(qrCodeMessage).matches();
        }
        return res;
    }

    private void switchGrabber(FFmpegFrameGrabber grabber, boolean doStop) {
        try {
            if (doStop) {
                grabber.stop();
            } else {
                grabber.start();
            }
        } catch (FrameGrabber.Exception e) {
            throw new VideoFileProcessingException(e);
        }
    }

    private void processPartOfVideoFile(FFmpegFrameGrabber frameGrabber,
                                        int partNum,
                                        int totalParts,
                                        CountDownLatch latch,
                                        QRCodeFoundCallback onQRCodeFound,
                                        VideoFileProgressCallback onVideoFileProgress) {

        var percentLast = -1;
        var converter = new Java2DFrameConverter();
        switchGrabber(frameGrabber, false);
        try {
            var lengthInFrames = frameGrabber.getLengthInFrames();
            var partSize = lengthInFrames / totalParts;
            var from = (partNum - 1) * partSize;
            var to = from + partSize;
            log.info("Start processing part of video file. Part num: {}/{}, frames in part: {}/{}, frame interval: {}-{}",
                    partNum, totalParts, partSize, lengthInFrames, from, to);

            frameGrabber.setFrameNumber(from);
            for (int i = from; i < to; i++) {

                var frame = frameGrabber.grabImage();

                if (i % GRAB_STEP == 0) {
                    var image = converter.convert(frame);
                    var qrCodeMessage = qrCodeService.readBarCode(image);

                    if (checkQRCodeMessage(qrCodeMessage) && onQRCodeFound != null) {
                        var entry = new QRCodeEntry(LocalTime.ofSecondOfDay(frame.timestamp / 1000000), qrCodeMessage);
                        onQRCodeFound.accept(entry);
                    }
                }

                if (onVideoFileProgress != null) {
                    var percent = (int) Math.floor((i - from) / (lengthInFrames / 100f));
                    if (percent != percentLast) {
                        var canFinish = onVideoFileProgress.apply(lengthInFrames, i, percent);
                        percentLast = percent;
                        if (canFinish) {
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            latch.countDown();
            switchGrabber(frameGrabber, true);
            throw new VideoFileProcessingException(e);
        }
        latch.countDown();
        switchGrabber(frameGrabber, true);
    }
}
