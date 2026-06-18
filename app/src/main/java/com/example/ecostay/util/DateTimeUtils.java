package com.example.ecostay.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateTimeUtils {

    private static final DateTimeFormatter ISO_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    private DateTimeUtils() {
    }

    public static String nowIso() {
        return LocalDateTime.now().format(ISO_DATE_TIME);
    }

    public static String formatDisplayDate(LocalDate date) {
        return date.format(DISPLAY_DATE);
    }

    public static String addDaysToDate(String dateStr, int days) {
        LocalDate date = LocalDate.parse(dateStr, DISPLAY_DATE);
        return date.plusDays(days).format(DISPLAY_DATE);
    }

    public static String combineDateAndTime(String date, String time) {
        return date + " " + time;
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) return false;
        try {
            LocalDate.parse(date.trim(), DISPLAY_DATE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidTime(String time) {
        if (time == null || time.trim().isEmpty()) return false;
        try {
            LocalTime.parse(time.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
