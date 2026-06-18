package com.example.ecostay.data;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.security.PasswordUtils;
import com.example.ecostay.session.SessionManager;

public final class AdminUserSeeder {

    public static final String ADMIN_EMAIL = "admin@techcare.com";
    public static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_PHONE = "0110000000";

    private AdminUserSeeder() {
    }

    public static void ensureAdminUser(@NonNull UserDao userDao) {
        UserEntity admin = userDao.findByEmailAndRole(ADMIN_EMAIL, SessionManager.ROLE_ADMIN);
        if (admin != null) {
            return;
        }

        UserEntity existing = userDao.findByEmail(ADMIN_EMAIL);
        if (existing != null) {
            existing.role = SessionManager.ROLE_ADMIN;
            PasswordUtils.SaltedHash hash = PasswordUtils.hashPassword(ADMIN_PASSWORD);
            existing.password = hash.hashBase64;
            existing.passwordSalt = hash.saltBase64;
            userDao.update(existing);
            return;
        }

        if (userDao.findByPhone(ADMIN_PHONE) != null) {
            return;
        }

        PasswordUtils.SaltedHash hash = PasswordUtils.hashPassword(ADMIN_PASSWORD);
        UserEntity user = new UserEntity();
        user.fullName = "TechCare Admin";
        user.email = ADMIN_EMAIL;
        user.phone = ADMIN_PHONE;
        user.password = hash.hashBase64;
        user.passwordSalt = hash.saltBase64;
        user.address = "TechCare Head Office";
        user.role = SessionManager.ROLE_ADMIN;
        userDao.insert(user);
    }

    public static void seedAdminUser(@NonNull SupportSQLiteDatabase db) {
        PasswordUtils.SaltedHash hash = PasswordUtils.hashPassword(ADMIN_PASSWORD);
        db.execSQL(
                "INSERT INTO users(fullName, email, phone, password, password_salt, address, role) " +
                        "SELECT ?, ?, ?, ?, ?, ?, ? WHERE NOT EXISTS (" +
                        "SELECT 1 FROM users WHERE email = ?)",
                new Object[]{
                        "TechCare Admin",
                        ADMIN_EMAIL,
                        ADMIN_PHONE,
                        hash.hashBase64,
                        hash.saltBase64,
                        "TechCare Head Office",
                        SessionManager.ROLE_ADMIN,
                        ADMIN_EMAIL
                }
        );
    }
}
