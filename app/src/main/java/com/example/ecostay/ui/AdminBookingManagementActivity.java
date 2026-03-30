package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.MainActivity;
import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.RoomBookingDao;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.dao.ServiceBookingDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.RoomBookingEntity;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.data.entity.ServiceBookingEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.AdminBookingListAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminBookingManagementActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private String bookingType = "ROOM";
    private TextInputEditText etBookingSearch;
    private Spinner spStatusFilter;
    private Spinner spSort;
    private RecyclerView rvBookings;
    private TextView tvEmptyState;
    private AdminBookingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin(this)) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_admin_booking_management);

        Button btnRoomsTab = findViewById(R.id.btnAdminManageRoomsTab);
        Button btnServicesTab = findViewById(R.id.btnAdminManageServicesTab);
        etBookingSearch = findViewById(R.id.etAdminManageBookingSearch);
        spStatusFilter = findViewById(R.id.spAdminManageBookingStatus);
        spSort = findViewById(R.id.spAdminManageBookingSort);
        rvBookings = findViewById(R.id.rvAdminManageBookings);
        tvEmptyState = findViewById(R.id.tvAdminManageBookingsEmpty);
        Button btnApply = findViewById(R.id.btnAdminManageBookingApplyFilters);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new String[]{"ALL", "CONFIRMED", "CANCELLED"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatusFilter.setAdapter(statusAdapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new String[]{"Newest first", "Oldest first", "Date ascending", "Date descending"}
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);

        adapter = new AdminBookingListAdapter(
                item -> {
                    Intent intent = new Intent(this, BookingDetailActivity.class);
                    intent.putExtra("bookingType", item.bookingType);
                    intent.putExtra("bookingId", item.bookingId);
                    startActivity(intent);
                },
                new AdminBookingListAdapter.OnBookingActionListener() {
                    @Override
                    public void onAcceptClicked(AdminBookingListAdapter.Item item) {
                        updateBookingStatus(item, "CONFIRMED");
                    }

                    @Override
                    public void onCancelClicked(AdminBookingListAdapter.Item item) {
                        updateBookingStatus(item, "CANCELLED");
                    }
                }
        );
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);

        btnRoomsTab.setOnClickListener(v -> {
            bookingType = "ROOM";
            btnRoomsTab.setEnabled(false);
            btnServicesTab.setEnabled(true);
            loadBookings();
        });
        btnServicesTab.setOnClickListener(v -> {
            bookingType = "SERVICE";
            btnRoomsTab.setEnabled(true);
            btnServicesTab.setEnabled(false);
            loadBookings();
        });
        btnApply.setOnClickListener(v -> loadBookings());

        btnRoomsTab.setEnabled(false);
        btnServicesTab.setEnabled(true);
        loadBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SessionManager.isAdmin(this)) {
            redirectToLogin();
            return;
        }
        loadBookings();
    }

    private void updateBookingStatus(AdminBookingListAdapter.Item item, String newStatus) {
        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomBookingDao roomBookingDao = database.roomBookingDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();

        dbExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            if ("ROOM".equals(item.bookingType)) {
                RoomBookingEntity booking = roomBookingDao.findById(item.bookingId);
                if (booking != null) {
                    booking.status = newStatus;
                    booking.updatedAtEpochMillis = now;
                    booking.cancelledAtEpochMillis = "CANCELLED".equals(newStatus) ? now : null;
                    roomBookingDao.update(booking);
                }
            } else {
                ServiceBookingEntity booking = serviceBookingDao.findById(item.bookingId);
                if (booking != null) {
                    booking.status = newStatus;
                    booking.updatedAtEpochMillis = now;
                    booking.cancelledAtEpochMillis = "CANCELLED".equals(newStatus) ? now : null;
                    serviceBookingDao.update(booking);
                }
            }
            runOnUiThread(this::loadBookings);
        });
    }

    private void loadBookings() {
        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomBookingDao roomBookingDao = database.roomBookingDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        ServiceDao serviceDao = database.serviceDao();

        String status = String.valueOf(spStatusFilter.getSelectedItem());
        String sortKey = getSortKey(spSort.getSelectedItemPosition(), bookingType);
        String query = etBookingSearch.getText() == null ? "" : etBookingSearch.getText().toString().trim().toLowerCase(Locale.getDefault());

        dbExecutor.execute(() -> {
            List<AdminBookingListAdapter.Item> items = new ArrayList<>();
            if ("ROOM".equals(bookingType)) {
                List<RoomBookingEntity> roomBookings = roomBookingDao.getAllManaged(status, sortKey);
                for (RoomBookingEntity booking : roomBookings) {
                    RoomTypeEntity roomType = roomTypeDao.findById(booking.roomTypeId);
                    String title = roomType == null ? getString(R.string.booking_unknown_room) : roomType.name;
                    AdminBookingListAdapter.Item item = new AdminBookingListAdapter.Item();
                    item.bookingId = booking.id;
                    item.bookingType = "ROOM";
                    item.title = title;
                    item.scheduleText = LocalDate.ofEpochDay(booking.startDateEpochDay).format(DISPLAY_FORMAT)
                            + " to " + LocalDate.ofEpochDay(booking.endDateEpochDay).format(DISPLAY_FORMAT);
                    item.status = booking.status;
                    item.paymentStatus = booking.paymentStatus;
                    item.totalAmount = booking.totalAmount;
                    if (matchesQuery(item, query)) {
                        items.add(item);
                    }
                }
            } else {
                List<ServiceBookingEntity> serviceBookings = serviceBookingDao.getAllManaged(status, sortKey);
                for (ServiceBookingEntity booking : serviceBookings) {
                    ServiceEntity service = serviceDao.findById(booking.serviceId);
                    String title = service == null ? getString(R.string.booking_unknown_service) : service.name;
                    AdminBookingListAdapter.Item item = new AdminBookingListAdapter.Item();
                    item.bookingId = booking.id;
                    item.bookingType = "SERVICE";
                    item.title = title;
                    item.scheduleText = LocalDate.ofEpochDay(booking.bookingDateEpochDay).format(DISPLAY_FORMAT);
                    item.status = booking.status;
                    item.paymentStatus = booking.paymentStatus;
                    item.totalAmount = booking.totalAmount;
                    if (matchesQuery(item, query)) {
                        items.add(item);
                    }
                }
            }

            runOnUiThread(() -> {
                adapter.setItems(items);
                boolean isEmpty = items.isEmpty();
                rvBookings.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
                tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            });
        });
    }

    private boolean matchesQuery(AdminBookingListAdapter.Item item, String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        if (String.valueOf(item.bookingId).contains(query)) {
            return true;
        }
        return item.title != null && item.title.toLowerCase(Locale.getDefault()).contains(query);
    }

    private String getSortKey(int selectedIndex, String type) {
        if (selectedIndex == 1) {
            return "created_asc";
        }
        if (selectedIndex == 2) {
            return "ROOM".equals(type) ? "checkin_asc" : "date_asc";
        }
        if (selectedIndex == 3) {
            return "ROOM".equals(type) ? "checkin_desc" : "date_desc";
        }
        return "created_desc";
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
