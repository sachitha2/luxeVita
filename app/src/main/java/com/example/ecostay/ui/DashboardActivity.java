package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        String name = SessionManager.getUserName(this);
        tvGreeting.setText(getString(R.string.dashboard_greeting, name));

        findViewById(R.id.cardBrowseServices).setOnClickListener(v ->
                startActivity(new Intent(this, BrowseServicesActivity.class)));
        findViewById(R.id.cardMyBookings).setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class)));
        findViewById(R.id.cardSavedDevices).setOnClickListener(v ->
                startActivity(new Intent(this, DeviceListActivity.class)));
        findViewById(R.id.cardFaqsTips).setOnClickListener(v ->
                startActivity(new Intent(this, FaqTipsActivity.class)));
        findViewById(R.id.cardSupport).setOnClickListener(v ->
                startActivity(new Intent(this, SupportActivity.class)));

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
