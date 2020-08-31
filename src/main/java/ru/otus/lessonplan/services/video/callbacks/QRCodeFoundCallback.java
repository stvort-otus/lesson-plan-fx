package ru.otus.lessonplan.services.video.callbacks;


import ru.otus.lessonplan.model.QRCodeEntry;

import java.util.function.Consumer;

public interface QRCodeFoundCallback extends Consumer<QRCodeEntry> {
}
