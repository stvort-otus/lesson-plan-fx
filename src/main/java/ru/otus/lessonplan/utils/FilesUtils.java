package ru.otus.lessonplan.utils;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public final class FilesUtils {
    private FilesUtils() {
    }

    public static String correctFileName(String fileName) {
        return fileName.replaceAll( "[\u0000-\u001f<>:\"/\\\\|?*=\u007f]+", "_" ).trim();
    }

    public static String getJarFolder() throws Exception {
        var codeSource = FilesUtils.class.getProtectionDomain().getCodeSource();
        URI uri;
        if (codeSource.getLocation() != null) {
            uri = codeSource.getLocation().toURI();
        } else {
            uri = FilesUtils.class.getResource(FilesUtils.class.getSimpleName() + ".class").toURI();
        }
        var path = uri.toString().replace("jar:", "").replace("file:", "");

        var i = path.indexOf("!");
        if (i >= 0) {
            path = path.substring(0, i);
        }

        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        var jarFile = new File(path);

        return jarFile.getParentFile().getAbsolutePath();
    }

    public static String getFileFullPathFromJarFolder(String fileName) {
        try {
            var path = Paths.get(getJarFolder(), fileName);
            var file = path.toFile();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
