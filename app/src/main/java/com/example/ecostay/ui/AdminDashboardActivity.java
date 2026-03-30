package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostay.MainActivity;
import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin(this)) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        Button btnManageBookings = findViewById(R.id.btnAdminManageBookings);
        Button btnManageServices = findViewById(R.id.btnAdminManageServices);
        Button btnManageRooms = findViewById(R.id.btnAdminManageRooms);
        Button btnLogout = findViewById(R.id.btnAdminLogout);

        btnManageBookings.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBookingManagementActivity.class)));
        btnManageServices.setOnClickListener(v ->
                startActivity(new Intent(this, AdminServicesActivity.class)));
        btnManageRooms.setOnClickListener(v ->
                startActivity(new Intent(this, AdminRoomsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
