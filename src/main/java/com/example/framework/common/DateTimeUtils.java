package com.example.framework.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
    }

    public static String now() {
        return LocalDateTime.now(DEFAULT_ZONE).format(DEFAULT_FORMATTER);
    }

    public static String format(Instant instant) {
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE).format(DEFAULT_FORMATTER);
    }
}
