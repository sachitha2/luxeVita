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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.notifications.NotificationScheduler;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.ui.adapters.RoomTypeAdapter;
import com.example.ecostay.ui.adapters.ServiceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private RecyclerView rvFeaturedRooms;
    private RecyclerView rvFeaturedServices;
    private RoomTypeAdapter featuredRoomsAdapter;
    private ServiceAdapter featuredServicesAdapter;

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

        rvFeaturedRooms = findViewById(R.id.rvFeaturedRooms);
        rvFeaturedServices = findViewById(R.id.rvFeaturedServices);

        setupFeaturedCarousels();

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

    private void setupFeaturedCarousels() {
        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        ServiceDao serviceDao = database.serviceDao();

        featuredRoomsAdapter = new RoomTypeAdapter(roomType -> {
            android.content.Intent intent = new android.content.Intent(HomeActivity.this, RoomBookingActivity.class);
            intent.putExtra("roomTypeId", roomType.id);
            startActivity(intent);
        });
        rvFeaturedRooms.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvFeaturedRooms.setAdapter(featuredRoomsAdapter);

        featuredServicesAdapter = new ServiceAdapter(service -> {
            android.content.Intent intent = new android.content.Intent(HomeActivity.this, ServiceBookingActivity.class);
            intent.putExtra("serviceId", service.id);
            startActivity(intent);
        });
        rvFeaturedServices.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvFeaturedServices.setAdapter(featuredServicesAdapter);

        dbExecutor.execute(() -> {
            List<RoomTypeEntity> allRooms = roomTypeDao.getAll();
            List<ServiceEntity> allServices = serviceDao.getAll();

            List<RoomTypeEntity> featuredRooms = new ArrayList<>();
            if (allRooms != null) {
                for (int i = 0; i < allRooms.size() && i < 5; i++) {
                    featuredRooms.add(allRooms.get(i));
                }
            }

            List<ServiceEntity> featuredServices = new ArrayList<>();
            if (allServices != null) {
                for (int i = 0; i < allServices.size() && i < 5; i++) {
                    featuredServices.add(allServices.get(i));
                }
            }

            runOnUiThread(() -> {
                featuredRoomsAdapter.setItems(featuredRooms);
                featuredServicesAdapter.setItems(featuredServices);
            });
        });
    }
}

