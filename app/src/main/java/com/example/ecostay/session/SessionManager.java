package com.example.ecostay.session;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {

    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_ADMIN = "ADMIN";

    private static final String PREFS_NAME = "techcare_session";
    private static final String KEY_USER_ID = "loggedInUserId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";

    private SessionManager() {
    }

    public static void saveSession(Context context, int userId, String fullName) {
        saveSession(context, userId, fullName, ROLE_CUSTOMER);
    }

    public static void saveSession(Context context, int userId, String fullName, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_NAME, fullName)
                .putString(KEY_USER_ROLE, role != null ? role : ROLE_CUSTOMER)
                .apply();
    }

    public static void updateUserName(Context context, String fullName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USER_NAME, fullName != null ? fullName : "")
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, "");
    }

    public static String getUserRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ROLE, ROLE_CUSTOMER);
    }

    public static boolean isAdmin(Context context) {
        return ROLE_ADMIN.equals(getUserRole(context));
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
