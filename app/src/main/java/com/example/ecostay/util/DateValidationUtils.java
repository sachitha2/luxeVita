package com.example.ecostay.util;

public final class DateValidationUtils {

    private DateValidationUtils() {
    }

    public static boolean isValidEpochDayRange(Long startEpochDay, Long endEpochDay) {
        if (startEpochDay == null || endEpochDay == null) return false;
        return startEpochDay < endEpochDay;
    }

    /**
     * Date range overlap for epoch-day values, treating ranges as [start, end) (end date checkout).
     */
    public static boolean rangesOverlap(long startA, long endA, long startB, long endB) {
        return startA < endB && startB < endA;
    }
}

