package ru.otus.lessonplan.services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.zxing.BarcodeFormat.QR_CODE;

@Service
public class QRCodeServiceImpl implements QRCodeService {

    @Override
    @SneakyThrows
    public String readBarCode(String filePath) {
        try (var is = Files.newInputStream(Paths.get(filePath))) {
            return readBarCode(is);
        }
    }

    @SneakyThrows
    @Override
    public String readBarCode(InputStream imageInputStream) {
        var bufferedImage = ImageIO.read(imageInputStream);
        return readBarCode(bufferedImage);
    }

    @Override
    public String readBarCode(BufferedImage bufferedImage) {
        var source = new BufferedImageLuminanceSource(bufferedImage);
        var bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Map<DecodeHintType, String> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            //hints.put(DecodeHintType.TRY_HARDER, "TRUE");

            var result = new MultiFormatReader().decode(bitmap, hints);

            return result.getText();
        } catch (NotFoundException ignored) {
        }
        return "";
    }

    @Override
    @SneakyThrows
    public void generateQRCodeImage(String text, int width, int height, OutputStream os) {
        var qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(text, QR_CODE, width, height, Map.of(EncodeHintType.CHARACTER_SET, "UTF-8"));
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
    }

    @Override
    @SneakyThrows
    public void generateQRCodeImageFile(String text, int width, int height, String filePath) {
        var qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(text, QR_CODE, width, height, Map.of(EncodeHintType.CHARACTER_SET, "UTF-8"));

        var path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }
}
