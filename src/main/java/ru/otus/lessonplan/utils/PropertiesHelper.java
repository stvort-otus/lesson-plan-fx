package ru.otus.lessonplan.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class PropertiesHelper {

    private PropertiesHelper() {
    }

    public static Properties loadPropertiesFile(String fileName) {
        Properties props = new Properties();

        File lastDirsFile = new File(fileName);
        // System.out.println(lastDirsFile.getAbsolutePath());
        if (lastDirsFile.exists()) {
            try (InputStreamReader ir = new InputStreamReader(new FileInputStream(lastDirsFile),
                    StandardCharsets.UTF_8)) {
                props.load(ir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    public static String getPropertyFromFile(String fileName, String key) {
        Properties props = loadPropertiesFile(fileName);
        return props.getProperty(key, "");
    }

    public static void updatePropertyFromFile(String fileName, String key, String value) {
        Properties props = loadPropertiesFile(fileName);
        props.setProperty(key, value);
        try (OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
            props.store(wr, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
