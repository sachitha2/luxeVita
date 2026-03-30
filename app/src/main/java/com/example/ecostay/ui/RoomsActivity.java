package com.example.ecostay.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.RoomBookingDao;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.RoomTypeAdapter;
import com.example.ecostay.util.DateValidationUtils;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomsActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private Spinner spRoomTypeFilter;
    private TextInputEditText etMinPrice;
    private TextInputEditText etMaxPrice;
    private Button btnPickStart;
    private Button btnPickEnd;
    private TextView tvDateRange;
    private Spinner spSort;
    private RecyclerView rvRooms;
    private TextView tvRoomsEmptyState;

    private RoomTypeAdapter adapter;

    private List<RoomTypeEntity> allRoomTypes = new ArrayList<>();
    private final List<Long> roomTypeIds = new ArrayList<>();

    private Long filterStartEpochDay;
    private Long filterEndEpochDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        // Session check (rooms booking needs a logged-in user)
        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        RoomBookingDao roomBookingDao = database.roomBookingDao();

        spRoomTypeFilter = findViewById(R.id.spRoomTypeFilter);
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        btnPickStart = findViewById(R.id.btnPickStart);
        btnPickEnd = findViewById(R.id.btnPickEnd);
        tvDateRange = findViewById(R.id.tvDateRange);
        spSort = findViewById(R.id.spSort);
        rvRooms = findViewById(R.id.rvRooms);
        tvRoomsEmptyState = findViewById(R.id.tvRoomsEmptyState);

        adapter = new RoomTypeAdapter(roomType -> {
            Intent intent = new Intent(RoomsActivity.this, RoomBookingActivity.class);
            intent.putExtra("roomTypeId", roomType.id);
            startActivity(intent);
        });

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);

        // Sort spinner
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Price: Low->High", "Price: High->Low", "Name: A->Z"}
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);

        btnPickStart.setOnClickListener(v -> showDatePicker(true));
        btnPickEnd.setOnClickListener(v -> showDatePicker(false));

        btnPickStart.setEnabled(true);
        btnPickEnd.setEnabled(true);

        // Load room types and set filter spinner.
        dbExecutor.execute(() -> {
            List<RoomTypeEntity> roomTypes = roomTypeDao.getAll();
            allRoomTypes = roomTypes;

            List<String> spinnerItems = new ArrayList<>();
            spinnerItems.add("All room types");
            roomTypeIds.clear();
            roomTypeIds.add(0L);

            for (RoomTypeEntity rt : roomTypes) {
                spinnerItems.add(rt.name);
                roomTypeIds.add(rt.id);
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                        RoomsActivity.this,
                        android.R.layout.simple_spinner_item,
                        spinnerItems
                );
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spRoomTypeFilter.setAdapter(adapterSpinner);
                adapter.setItems(allRoomTypes);
                updateEmptyState(allRoomTypes);
            });
        });

        View.OnClickListener applyFilters = v -> {
            // Read filters on UI thread.
            Double minPrice = parseNullableDouble(etMinPrice.getText() == null ? null : etMinPrice.getText().toString().trim());
            Double maxPrice = parseNullableDouble(etMaxPrice.getText() == null ? null : etMaxPrice.getText().toString().trim());

            int selectedSortPos = spSort.getSelectedItemPosition();
            int roomTypePos = spRoomTypeFilter.getSelectedItemPosition();
            long selectedRoomTypeId = roomTypePos >= 0 && roomTypePos < roomTypeIds.size() ? roomTypeIds.get(roomTypePos) : 0L;

            if (filterStartEpochDay != null && filterEndEpochDay != null && !DateValidationUtils.isValidEpochDayRange(filterStartEpochDay, filterEndEpochDay)) {
                toast("Invalid date range: start must be before end.");
                return;
            }

            Long start = filterStartEpochDay;
            Long end = filterEndEpochDay;

            dbExecutor.execute(() -> {
                List<RoomTypeEntity> results = new ArrayList<>();

                for (RoomTypeEntity rt : allRoomTypes) {
                    if (selectedRoomTypeId != 0L && rt.id != selectedRoomTypeId) continue;

                    if (minPrice != null && rt.pricePerNight < minPrice) continue;
                    if (maxPrice != null && rt.pricePerNight > maxPrice) continue;

                    if (start != null && end != null) {
                        int overlap = roomBookingDao.countOverlappingConfirmed(rt.id, start, end);
                        int available = rt.totalRooms - overlap;
                        if (available <= 0) continue;
                    }

                    results.add(rt);
                }

                // Sort
                Comparator<RoomTypeEntity> comparator;
                if (selectedSortPos == 0) {
                    comparator = Comparator.comparingDouble(r -> r.pricePerNight);
                } else if (selectedSortPos == 1) {
                    comparator = (a, b) -> Double.compare(b.pricePerNight, a.pricePerNight);
                } else {
                    comparator = Comparator.comparing(r -> r.name, String.CASE_INSENSITIVE_ORDER);
                }
                Collections.sort(results, comparator);

                runOnUiThread(() -> {
                    adapter.setItems(results);
                    updateEmptyState(results);
                });
            });
        };

        Button btnApplyFilters = findViewById(R.id.btnApplyFilters);
        btnApplyFilters.setOnClickListener(applyFilters);
    }

    private void showDatePicker(boolean isStart) {
        LocalDate initial = LocalDate.now();
        if (isStart && filterStartEpochDay != null) initial = LocalDate.ofEpochDay(filterStartEpochDay);
        if (!isStart && filterEndEpochDay != null) initial = LocalDate.ofEpochDay(filterEndEpochDay);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
            if (isStart) {
                filterStartEpochDay = picked.toEpochDay();
            } else {
                filterEndEpochDay = picked.toEpochDay();
            }
            updateDateRangeText();
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        dialog.show();
    }

    private void updateDateRangeText() {
        if (filterStartEpochDay == null || filterEndEpochDay == null) {
            tvDateRange.setText("No date range selected");
            return;
        }

        String start = LocalDate.ofEpochDay(filterStartEpochDay).format(DISPLAY_FORMAT);
        String end = LocalDate.ofEpochDay(filterEndEpochDay).format(DISPLAY_FORMAT);
        tvDateRange.setText(start + " to " + end + " (availability)");
    }

    private Double parseNullableDouble(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyState(List<RoomTypeEntity> rooms) {
        if (tvRoomsEmptyState == null) return;
        boolean isEmpty = rooms == null || rooms.isEmpty();
        tvRoomsEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}

