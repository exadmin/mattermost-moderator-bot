package com.github.exadmin.aibot.utils;

public class StrUtils {
    public static boolean hasNoValueOneOf(String ... str) {
        for (String next : str) {
            if (next == null || next.isBlank()) return true;
        }

        return false;
    }
}
