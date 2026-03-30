package com.example.ecostay;

import com.example.ecostay.security.PasswordUtils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PasswordUtilsTest {

    @Test
    public void hashAndVerifyPassword_roundTrip() {
        String password = "SecurePassword#123";
        PasswordUtils.SaltedHash saltedHash = PasswordUtils.hashPassword(password);

        assertNotNull(saltedHash);
        assertNotNull(saltedHash.saltBase64);
        assertNotNull(saltedHash.hashBase64);

        assertTrue(PasswordUtils.verifyPassword(password, saltedHash.saltBase64, saltedHash.hashBase64));
        assertFalse(PasswordUtils.verifyPassword("WrongPassword", saltedHash.saltBase64, saltedHash.hashBase64));
    }
}

