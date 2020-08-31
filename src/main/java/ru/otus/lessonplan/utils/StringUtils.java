package ru.otus.lessonplan.utils;

public final class StringUtils {
    private StringUtils() {
    }

    private static final String UTF8_BOM = "\uFEFF";

    public static String withoutBom(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
}
