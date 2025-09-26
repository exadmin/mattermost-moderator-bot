package com.github.exadmin.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

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

    public static String getCurrentDateTimeStr()  {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static final Random rnd = new SecureRandom();

    public static long getRandomLong(long bound) {
        return rnd.nextLong(bound);
    }
}
