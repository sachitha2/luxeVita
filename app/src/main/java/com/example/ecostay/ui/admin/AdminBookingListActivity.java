package com.example.ecostay.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.model.AdminBookingSummary;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.adapters.AdminBookingAdapter;
import com.example.ecostay.ui.admin.viewmodel.AdminBookingViewModel;
import com.example.ecostay.util.ToolbarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminBookingListActivity extends AppCompatActivity {

    private AdminBookingViewModel bookingViewModel;
    private AdminBookingAdapter adapter;
    private String currentStatusFilter = "All";
    private List<AdminBookingSummary> allBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_booking_list);
        ToolbarUtils.setupBackToolbar(this, R.string.admin_bookings_title);
        bookingViewModel = new ViewModelProvider(this).get(AdminBookingViewModel.class);

        Spinner spStatusFilter = findViewById(R.id.spStatusFilter);
        SearchView searchView = findViewById(R.id.searchBookings);
        RecyclerView rvBookings = findViewById(R.id.rvBookings);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.admin_booking_statuses, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatusFilter.setAdapter(filterAdapter);

        adapter = new AdminBookingAdapter(booking -> {
            Intent intent = new Intent(this, AdminBookingDetailsActivity.class);
            intent.putExtra(AdminBookingDetailsActivity.EXTRA_BOOKING_ID, booking.bookingId);
            startActivity(intent);
        });
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);

        spStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatusFilter = parent.getItemAtPosition(position).toString();
                bookingViewModel.loadAllBookings(currentStatusFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applySearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applySearch(newText);
                return true;
            }
        });

        bookingViewModel.getAdminBookings().observe(this, bookings -> {
            allBookings = bookings != null ? bookings : new ArrayList<>();
            adapter.setItems(allBookings);
            boolean empty = allBookings.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        bookingViewModel.loadAllBookings(currentStatusFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bookingViewModel.loadAllBookings(currentStatusFilter);
    }

    private void applySearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setItems(allBookings);
            return;
        }
        String lower = query.trim().toLowerCase(Locale.getDefault());
        List<AdminBookingSummary> filtered = new ArrayList<>();
        for (AdminBookingSummary booking : allBookings) {
            if (String.valueOf(booking.bookingId).contains(lower)
                    || booking.customerName.toLowerCase(Locale.getDefault()).contains(lower)
                    || booking.deviceLabel.toLowerCase(Locale.getDefault()).contains(lower)) {
                filtered.add(booking);
            }
        }
        adapter.setItems(filtered);
    }
}
