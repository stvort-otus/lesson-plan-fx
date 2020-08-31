package ru.otus.lessonplan.services;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

public interface QRCodeService {
    String readBarCode(String filePath);

    String readBarCode(InputStream imageInputStream);

    String readBarCode(BufferedImage bufferedImage);

    void generateQRCodeImage(String text, int width, int height, OutputStream os);

    void generateQRCodeImageFile(String text, int width, int height, String filePath);
}
