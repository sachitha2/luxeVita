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
    private String currentSearchQuery = "";

    private TextView tvEmpty;
    private TextView tvResultsCount;
    private View ivEmpty;
    private RecyclerView rvBookings;

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
        rvBookings = findViewById(R.id.rvBookings);
        tvEmpty = findViewById(R.id.tvEmpty);
        ivEmpty = findViewById(R.id.ivEmpty);
        tvResultsCount = findViewById(R.id.tvResultsCount);

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.admin_booking_statuses, R.layout.item_spinner_category);
        filterAdapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
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
                currentSearchQuery = query != null ? query : "";
                refreshDisplayedBookings();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText != null ? newText : "";
                refreshDisplayedBookings();
                return true;
            }
        });

        bookingViewModel.getAdminBookings().observe(this, bookings -> {
            allBookings = bookings != null ? bookings : new ArrayList<>();
            refreshDisplayedBookings();
        });

        bookingViewModel.loadAllBookings(currentStatusFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bookingViewModel.loadAllBookings(currentStatusFilter);
    }

    private void refreshDisplayedBookings() {
        List<AdminBookingSummary> displayed = filterBookings(allBookings, currentSearchQuery);
        adapter.setItems(displayed);
        updateEmptyState(displayed.isEmpty());
        updateResultsCount(displayed.size());
    }

    private List<AdminBookingSummary> filterBookings(List<AdminBookingSummary> source, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(source);
        }
        String lower = query.trim().toLowerCase(Locale.getDefault());
        List<AdminBookingSummary> filtered = new ArrayList<>();
        for (AdminBookingSummary booking : source) {
            if (String.valueOf(booking.bookingId).contains(lower)
                    || booking.customerName.toLowerCase(Locale.getDefault()).contains(lower)
                    || booking.deviceLabel.toLowerCase(Locale.getDefault()).contains(lower)) {
                filtered.add(booking);
            }
        }
        return filtered;
    }

    private void updateEmptyState(boolean empty) {
        int emptyVisibility = empty ? View.VISIBLE : View.GONE;
        tvEmpty.setVisibility(emptyVisibility);
        ivEmpty.setVisibility(emptyVisibility);
        rvBookings.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            tvResultsCount.setVisibility(View.GONE);
        }
    }

    private void updateResultsCount(int count) {
        if (count == 0) {
            return;
        }
        tvResultsCount.setText(getString(R.string.admin_bookings_count, count));
        tvResultsCount.setVisibility(View.VISIBLE);
    }
}
