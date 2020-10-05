package ru.otus.lessonplan.services.video;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.otus.lessonplan.config.VideoFileQRCodeExtractorProps;
import ru.otus.lessonplan.model.QRCodeEntry;
import ru.otus.lessonplan.services.QRCodeService;
import ru.otus.lessonplan.services.video.callbacks.QRCodeFoundCallback;
import ru.otus.lessonplan.services.video.callbacks.VideoFileProgressCallback;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Slf4j
@Service
public class VideoFileQRCodeExtractorOpenCV implements VideoFileQRCodeExtractor {
    //private static final int GRAB_STEP = 48;
    private static final Pattern BAD_QR_CODE_TEXT_PATTERN =
            Pattern.compile("^(\\d|~|@|#|%|\\^|&|\\*|\\(|\\)|-|=|\\+|_|\\$|!|\"|:|;|,|\\.|<|>|\\?|\\|/|№|\\r|\\n)+$");
    private final VideoFileQRCodeExtractorProps props;
    private final FFmpegFrameGrabberFactory grabberFactory;
    private final QRCodeService qrCodeService;

    public VideoFileQRCodeExtractorOpenCV(
            VideoFileQRCodeExtractorProps props,
            FFmpegFrameGrabberFactory grabberFactory,
            QRCodeService qrCodeService) {

        log.info("PROPS: {}", props);

        this.props = props;
        this.grabberFactory = grabberFactory;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public void processVideoFile(String fileName, QRCodeFoundCallback onQRCodeFound,
                                 VideoFileProgressCallback onVideoFileProgress) {

        log.info("Start processing video file: {}", fileName);
        var t = System.currentTimeMillis();

        var exceptions = Collections.synchronizedList(new ArrayList<Exception>());
        //var latch = new CountDownLatch(degreeOfParallelism);
        var latch = new CountDownLatch(props.getDegreeOfParallelism());
        try {
            //var pool = Executors.newFixedThreadPool(degreeOfParallelism);
            var pool = Executors.newFixedThreadPool(props.getDegreeOfParallelism());

            HashMap<String, Integer> progressMap = new HashMap<>();
            VideoFileProgressCallback progressCallback = prepareVideoFileProgressCallback(progressMap, onVideoFileProgress);

            submitVideoFilePartsProcessing(pool, fileName, onQRCodeFound, progressCallback, latch, exceptions);

            awaitAllAndShutdownThreadPool(pool, latch);

            exceptions.stream().findAny().ifPresent(e -> {
                throw new VideoFileProcessingException(e);
            });

        } catch (Exception e) {
            log.error("Error during processing video file", e);
            throw new VideoFileProcessingException(e);
        }

        long duration = System.currentTimeMillis() - t;
        log.info("Finished video file processing. Duration: {} ({}) ", duration,
                LocalTime.ofSecondOfDay(duration / 1000).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    @Override
    public void processVideoFile(String fileName, QRCodeFoundCallback onQRCodeFound) {
        processVideoFile(fileName, onQRCodeFound, null);
    }

    private VideoFileProgressCallback prepareVideoFileProgressCallback(HashMap<String, Integer> progressMap,
                                                                       VideoFileProgressCallback parentCallback){
        return (totalFrames, currentFrame, percent) -> {
            synchronized (progressMap) {
                log.debug("Thread process percent {} = {} ", Thread.currentThread().getName(), percent);
                progressMap.put(Thread.currentThread().getName(), percent);
                int sum = progressMap.values().stream().mapToInt(Integer::intValue).sum();
                if (parentCallback != null) {
                    return parentCallback.apply(totalFrames, currentFrame, sum);
                }
            }
            return false;
        };
    }

    private void submitVideoFilePartsProcessing(ExecutorService pool, String fileName, QRCodeFoundCallback onQRCodeFound,
                                                VideoFileProgressCallback progressCallback, CountDownLatch latch, List<Exception> exceptions){
        IntStream.range(1, props.getDegreeOfParallelism() + 1)
                .forEachOrdered(i -> pool.submit(() -> processPartOfVideoFile(fileName, i, onQRCodeFound, progressCallback, latch, exceptions)));
    }

    private void awaitAllAndShutdownThreadPool(ExecutorService pool, CountDownLatch latch) throws InterruptedException {
        latch.await();

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        pool.shutdownNow();

    }

    private boolean checkQRCodeMessage(String qrCodeMessage) {
        var res = !"".equals(qrCodeMessage);
        if (res && props.isIgnoreQrCodeTextWithNumbersOnly()) {
            return !BAD_QR_CODE_TEXT_PATTERN.matcher(qrCodeMessage).matches();
        }
        return res;
    }

    private void switchGrabber(FFmpegFrameGrabber grabber, boolean doStop) {
        log.info("Grabber switched, thread: {}, doStop: {}", Thread.currentThread().getName(), doStop);
        try {
            if (doStop) {
                grabber.close();
            } else {
                grabber.start();
            }
        } catch (FrameGrabber.Exception e) {
            log.error("Error during grabber switching", e);
            throw new VideoFileProcessingException(e);
        }
    }

    private void processPartOfVideoFile(String fileName, int partNum, QRCodeFoundCallback onQRCodeFound,
                                        VideoFileProgressCallback onVideoFileProgress,
                                        CountDownLatch latch, List<Exception> exceptions) {

        var percentLast = -1;
        var frameGrabber = grabberFactory.createFrameGrabber(fileName);
        var converter = new Java2DFrameConverter();
        switchGrabber(frameGrabber, false);
        try {
            var lengthInFrames = frameGrabber.getLengthInFrames();
            var partSize = lengthInFrames / props.getDegreeOfParallelism();
            var from = (partNum - 1) * partSize;
            var to = from + partSize;
            log.info("Start processing part of video file. Part num: {}/{}, frames in part: {}/{}, frame interval: {}-{}",
                    partNum, props.getDegreeOfParallelism(), partSize, lengthInFrames, from, to);

            frameGrabber.setFrameNumber(from);
            for (int i = from; i < to; i++) {

                if (i % props.getGrabStepFrames() == 0) {
                    var frame = frameGrabber.grabImage();
                    var image = converter.convert(frame);
                    var qrCodeMessage = qrCodeService.readBarCode(image);
                    log.debug("Processing frame. Thread: {}, frame: {}, frames: {}/{}", Thread.currentThread().getName(), i, i - from, to - from);

                    if (checkQRCodeMessage(qrCodeMessage) && onQRCodeFound != null) {
                        var entry = new QRCodeEntry(LocalTime.ofSecondOfDay(frame.timestamp / 1000000), qrCodeMessage);
                        log.debug("QR-code founded, entry: {}", entry);
                        onQRCodeFound.accept(entry);
                        log.debug("Entry processed");
                    }
                } else {
                    frameGrabber.grabFrame(false, true, false, false, false);
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
            log.error("Error during processing video file part", e);
            exceptions.add(e);
        }
        latch.countDown();
        switchGrabber(frameGrabber, true);
    }
}
