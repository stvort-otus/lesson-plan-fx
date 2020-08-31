package ru.otus.lessonplan.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@Data
public class QRCodeEntry {
    private LocalTime time;
    private String message;

    @Override
    public String toString() {
        return "QRCodeEntry{" +
                "time=" + time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                ", message='" + message + '\'' +
                '}';
    }
}
