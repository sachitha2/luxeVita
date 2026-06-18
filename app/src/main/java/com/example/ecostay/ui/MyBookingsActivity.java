package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.model.BookingSummary;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.BookingAdapter;
import com.example.ecostay.ui.viewmodel.BookingViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class MyBookingsActivity extends AppCompatActivity {

    private BookingViewModel bookingViewModel;
    private BookingAdapter adapter;
    private TextView tvEmpty;
    private View ivEmpty;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        userId = SessionManager.getUserId(this);
        setContentView(R.layout.activity_my_bookings);
        ToolbarUtils.setupBackToolbar(this, R.string.my_bookings_title);

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        RecyclerView rvBookings = findViewById(R.id.rvBookings);
        tvEmpty = findViewById(R.id.tvEmpty);
        ivEmpty = findViewById(R.id.ivEmpty);

        adapter = new BookingAdapter(booking -> {
            Intent intent = new Intent(this, BookingDetailsActivity.class);
            intent.putExtra(BookingDetailsActivity.EXTRA_BOOKING_ID, booking.bookingId);
            startActivity(intent);
        });

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);

        bookingViewModel.getBookings().observe(this, bookings -> {
            boolean empty = bookings == null || bookings.isEmpty();
            int visibility = empty ? View.VISIBLE : View.GONE;
            tvEmpty.setVisibility(visibility);
            ivEmpty.setVisibility(visibility);
            adapter.setItems(bookings);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bookingViewModel.loadBookings(userId);
    }
}
