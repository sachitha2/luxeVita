package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.model.DashboardStats;
import com.example.ecostay.data.repository.BookingRepository;

public class AdminDashboardViewModel extends AndroidViewModel {

    private final BookingRepository bookingRepository;

    public AdminDashboardViewModel(@NonNull Application application) {
        super(application);
        bookingRepository = new BookingRepository(application);
    }

    public LiveData<DashboardStats> getDashboardStats() {
        return bookingRepository.getDashboardStats();
    }

    public void loadDashboardStats() {
        bookingRepository.loadDashboardStats();
    }
}
