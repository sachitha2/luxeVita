package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (SessionManager.isLoggedIn(SplashActivity.this)) {
                if (SessionManager.isAdmin(SplashActivity.this)) {
                    intent = new Intent(SplashActivity.this, com.example.ecostay.ui.admin.AdminDashboardActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, DashboardActivity.class);
                }
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
