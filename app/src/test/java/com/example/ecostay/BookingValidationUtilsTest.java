package com.example.ecostay;

import com.example.ecostay.util.BookingValidationUtils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BookingValidationUtilsTest {

    @Test
    public void isRoomAvailable_overlapLessThanInventory() {
        assertTrue(BookingValidationUtils.isRoomAvailable(0, 3));
        assertTrue(BookingValidationUtils.isRoomAvailable(2, 3));
        assertFalse(BookingValidationUtils.isRoomAvailable(3, 3));
        assertFalse(BookingValidationUtils.isRoomAvailable(4, 3));
    }

    @Test
    public void isServiceDateAvailable_onlyWhenNoConfirmedBookings() {
        assertTrue(BookingValidationUtils.isServiceDateAvailable(0));
        assertFalse(BookingValidationUtils.isServiceDateAvailable(1));
        assertFalse(BookingValidationUtils.isServiceDateAvailable(10));
    }
}

