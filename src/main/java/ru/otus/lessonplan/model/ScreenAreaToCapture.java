package ru.otus.lessonplan.model;

import lombok.Data;

@Data
public class ScreenAreaToCapture {
    private int x;
    private int y;
    private int width;
    private int height;

    public ScreenAreaToCapture(double x, double y, double width, double height) {
        this.x = (int)Math.round(x);
        this.y = (int)Math.round(y);
        this.width = (int)Math.round(width);
        this.height = (int)Math.round(height);
    }
}
