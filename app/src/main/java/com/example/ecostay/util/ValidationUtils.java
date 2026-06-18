package com.example.ecostay.util;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    private ValidationUtils() {
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isValidIssueDescription(String description) {
        return description != null && description.trim().length() >= 10;
    }

    public static boolean isValidPrice(String priceStr) {
        if (isEmpty(priceStr)) {
            return false;
        }
        try {
            double price = Double.parseDouble(priceStr.trim());
            return price > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
