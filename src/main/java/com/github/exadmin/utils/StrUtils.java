package com.github.exadmin.utils;

import java.util.List;

public class StrUtils {
    public static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean hasValue(List<String> values) {
        return values != null && !values.isEmpty() && hasValue(values.getFirst());
    }
}
