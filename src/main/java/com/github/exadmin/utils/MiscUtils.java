package com.github.exadmin.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MiscUtils {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // do nothing
        }
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Returns current time formatted by DATE_TIME_FORMATTER
     * @return String
     */
    public static String getCurrentTimeStr() {
        LocalTime currentTime = LocalTime.now();
        return currentTime.format(DATE_TIME_FORMATTER);
    }
}
