package com.example.ecostay.util;

public final class BookingValidationUtils {

    private BookingValidationUtils() {
    }

    public static boolean isRoomAvailable(int overlappingConfirmedBookings, int totalRooms) {
        return totalRooms > overlappingConfirmedBookings;
    }

    /**
     * Services are modeled as 1 confirmed reservation per service per date in this demo.
     */
    public static boolean isServiceDateAvailable(int confirmedCountForDate) {
        return confirmedCountForDate <= 0;
    }
}

