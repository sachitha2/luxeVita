package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.entity.RepairStatusEntity;
import com.example.ecostay.data.model.AdminBookingDetail;
import com.example.ecostay.data.model.AdminBookingSummary;
import com.example.ecostay.data.repository.BookingRepository;
import com.example.ecostay.data.repository.BookingRepository.OperationResult;

import java.util.List;

public class AdminBookingViewModel extends AndroidViewModel {

    private final BookingRepository bookingRepository;

    public AdminBookingViewModel(@NonNull Application application) {
        super(application);
        bookingRepository = new BookingRepository(application);
    }

    public LiveData<List<AdminBookingSummary>> getAdminBookings() {
        return bookingRepository.getAdminBookings();
    }

    public LiveData<AdminBookingDetail> getAdminBookingDetail() {
        return bookingRepository.getAdminBookingDetail();
    }

    public LiveData<List<RepairStatusEntity>> getStatusHistory() {
        return bookingRepository.getStatusHistory();
    }

    public LiveData<OperationResult> getAdminUpdateResult() {
        return bookingRepository.getAdminUpdateResult();
    }

    public void loadAllBookings(String statusFilter) {
        bookingRepository.loadAllBookings(statusFilter);
    }

    public void loadAdminBookingDetail(int bookingId) {
        bookingRepository.loadAdminBookingDetail(bookingId);
    }

    public void updateBookingAsAdmin(int bookingId, String status, String technicianName,
                                     String estimatedCompletion, String remarks, String updatedBy) {
        bookingRepository.updateBookingAsAdmin(
                bookingId, status, technicianName, estimatedCompletion, remarks, updatedBy);
    }
}
