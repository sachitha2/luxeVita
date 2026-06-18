package com.example.ecostay.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.model.DashboardStats;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.viewmodel.AdminDashboardViewModel;

public class AdminDashboardActivity extends AppCompatActivity {

    private AdminDashboardViewModel dashboardViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);
        dashboardViewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        tvGreeting.setText(getString(R.string.admin_dashboard_greeting,
                SessionManager.getUserName(this)));

        setupNavCard(R.id.cardBookings, R.id.navBookings,
                R.drawable.ic_nav_bookings, R.string.admin_card_bookings,
                R.string.admin_card_bookings_desc, AdminBookingListActivity.class);
        setupNavCard(R.id.cardServices, R.id.navServices,
                R.drawable.ic_nav_services, R.string.admin_card_services,
                R.string.admin_card_services_desc, ManageServicesActivity.class);
        setupNavCard(R.id.cardFaqs, R.id.navFaqs,
                R.drawable.ic_nav_faq, R.string.admin_card_faqs,
                R.string.admin_card_faqs_desc, ManageFaqActivity.class);
        setupNavCard(R.id.cardTips, R.id.navTips,
                R.drawable.ic_lightbulb_tip, R.string.admin_card_tips,
                R.string.admin_card_tips_desc, ManageTipsActivity.class);

        dashboardViewModel.getDashboardStats().observe(this, this::showStats);
        dashboardViewModel.loadDashboardStats();

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            Intent intent = new Intent(this, AdminLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dashboardViewModel != null) {
            dashboardViewModel.loadDashboardStats();
        }
    }

    private void showStats(DashboardStats stats) {
        if (stats == null) return;
        ((TextView) findViewById(R.id.tvStatTotal)).setText(String.valueOf(stats.totalBookings));
        ((TextView) findViewById(R.id.tvStatReceived)).setText(String.valueOf(stats.receivedCount));
        ((TextView) findViewById(R.id.tvStatAssigned)).setText(String.valueOf(stats.technicianAssignedCount));
        ((TextView) findViewById(R.id.tvStatUnderRepair)).setText(String.valueOf(stats.underRepairCount));
        ((TextView) findViewById(R.id.tvStatReady)).setText(String.valueOf(stats.readyForPickupCount));
        ((TextView) findViewById(R.id.tvStatCompleted)).setText(String.valueOf(stats.completedCount));
        ((TextView) findViewById(R.id.tvStatCancelled)).setText(String.valueOf(stats.cancelledCount));
    }

    private void setupNavCard(int cardId, int contentRootId, int iconRes, int titleRes,
                              int descRes, Class<?> targetActivity) {
        findViewById(cardId).setOnClickListener(v ->
                startActivity(new Intent(this, targetActivity)));

        android.view.View content = findViewById(contentRootId);
        ImageView icon = content.findViewById(R.id.ivNavIcon);
        TextView title = content.findViewById(R.id.tvNavTitle);
        TextView desc = content.findViewById(R.id.tvNavDesc);
        icon.setImageResource(iconRes);
        title.setText(titleRes);
        desc.setText(descRes);
    }
}
