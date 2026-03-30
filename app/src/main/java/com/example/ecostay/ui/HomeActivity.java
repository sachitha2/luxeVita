package com.example.ecostay.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.notifications.NotificationScheduler;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // If session is missing, go back to login.
        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }

        // Request notification permission on Android 13+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS
                );
            }
        }

        // Schedule periodic promotions (unique work so it won't duplicate).
        NotificationScheduler.schedulePeriodicPromotions(this);

        View shortcutAvailableRooms = findViewById(R.id.shortcutAvailableRooms);
        View shortcutAvailableServices = findViewById(R.id.shortcutAvailableServices);
        Button btnOffers = findViewById(R.id.btnOffers);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnBookings = findViewById(R.id.btnBookings);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        shortcutAvailableRooms.setOnClickListener(v -> startActivity(new android.content.Intent(this, RoomsActivity.class)));
        shortcutAvailableServices.setOnClickListener(v -> startActivity(new android.content.Intent(this, ServicesActivity.class)));
        btnOffers.setOnClickListener(v -> startActivity(new android.content.Intent(this, OffersActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new android.content.Intent(this, ProfileActivity.class)));
        btnBookings.setOnClickListener(v -> startActivity(new android.content.Intent(this, BookingManagementActivity.class)));

        btnSignOut.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            finish();
        });
    }
}

