package com.example.ecostay;

import com.example.ecostay.util.DateValidationUtils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateValidationUtilsTest {

    @Test
    public void isValidEpochDayRange_valid() {
        assertTrue(DateValidationUtils.isValidEpochDayRange(10L, 11L));
    }

    @Test
    public void isValidEpochDayRange_invalid() {
        assertFalse(DateValidationUtils.isValidEpochDayRange(11L, 10L));
        assertFalse(DateValidationUtils.isValidEpochDayRange(10L, 10L));
        assertFalse(DateValidationUtils.isValidEpochDayRange(null, 10L));
        assertFalse(DateValidationUtils.isValidEpochDayRange(10L, null));
    }

    @Test
    public void rangesOverlap_treatedAsStartEndCheckout() {
        // [1,5) and [5,8) do not overlap (end is checkout day)
        assertFalse(DateValidationUtils.rangesOverlap(1L, 5L, 5L, 8L));

        // [1,5) and [4,8) overlap
        assertTrue(DateValidationUtils.rangesOverlap(1L, 5L, 4L, 8L));
    }
}

