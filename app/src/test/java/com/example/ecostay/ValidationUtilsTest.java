package com.example.ecostay;

import com.example.ecostay.util.ValidationUtils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationUtilsTest {

    @Test
    public void isValidEmail_acceptsValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("user@techcare.lk"));
    }

    @Test
    public void isValidEmail_rejectsInvalidEmail() {
        assertFalse(ValidationUtils.isValidEmail("not-an-email"));
    }

    @Test
    public void isValidPhone_acceptsTenDigits() {
        assertTrue(ValidationUtils.isValidPhone("0771234567"));
    }

    @Test
    public void isValidPhone_rejectsShortPhone() {
        assertFalse(ValidationUtils.isValidPhone("12345"));
    }

    @Test
    public void isValidPassword_requiresEightChars() {
        assertTrue(ValidationUtils.isValidPassword("password1"));
        assertFalse(ValidationUtils.isValidPassword("short"));
    }

    @Test
    public void isValidIssueDescription_requiresTenChars() {
        assertTrue(ValidationUtils.isValidIssueDescription("Screen is cracked badly"));
        assertFalse(ValidationUtils.isValidIssueDescription("short"));
    }
}
