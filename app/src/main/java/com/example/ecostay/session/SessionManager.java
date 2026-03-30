package com.example.ecostay.session;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {

    private static final String PREFS_NAME = "ecostay_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_ADMIN = "is_admin";

    private SessionManager() {
    }

    public static void saveSession(Context context, long userId, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_IS_ADMIN, false)
                .apply();
    }

    public static void saveAdminSession(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_IS_ADMIN, true)
                .remove(KEY_USER_ID)
                .apply();
    }

    public static Long getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.contains(KEY_USER_ID)) return null;
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public static String getEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, null);
    }

    public static boolean isAdmin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}

