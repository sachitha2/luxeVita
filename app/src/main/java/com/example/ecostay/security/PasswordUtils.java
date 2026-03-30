package com.example.ecostay.security;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing helper using PBKDF2 with a per-user random salt.
 *
 * Stored values are Base64 strings: passwordSalt + passwordHash.
 */
public final class PasswordUtils {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 10_000;
    private static final int KEY_LENGTH_BITS = 256;

    private PasswordUtils() {
    }

    public static SaltedHash hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt);
        return new SaltedHash(
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash)
        );
    }

    public static boolean verifyPassword(String password, String saltBase64, String expectedHashBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] expected = Base64.getDecoder().decode(expectedHashBase64);

        byte[] actual = pbkdf2(password.toCharArray(), salt);
        if (actual.length != expected.length) return false;

        // Constant-time compare is not strictly necessary for demo apps,
        // but we do a simple constant-time-ish loop anyway.
        int diff = 0;
        for (int i = 0; i < actual.length; i++) {
            diff |= (actual[i] ^ expected[i]);
        }
        return diff == 0;
    }

    private static byte[] pbkdf2(char[] passwordChars, byte[] salt) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH_BITS);
            return keyFactory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    public static final class SaltedHash {
        public final String saltBase64;
        public final String hashBase64;

        public SaltedHash(String saltBase64, String hashBase64) {
            this.saltBase64 = saltBase64;
            this.hashBase64 = hashBase64;
        }
    }
}

