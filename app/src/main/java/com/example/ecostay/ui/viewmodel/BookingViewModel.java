package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.entity.BookingEntity;
import com.example.ecostay.data.entity.RepairStatusEntity;
import com.example.ecostay.data.model.BookingSummary;
import com.example.ecostay.data.repository.BookingRepository;

import java.util.List;

public class BookingViewModel extends AndroidViewModel {

    private final BookingRepository bookingRepository;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        bookingRepository = new BookingRepository(application);
    }

    public LiveData<List<BookingSummary>> getBookings() {
        return bookingRepository.getBookings();
    }

    public LiveData<BookingEntity> getBookingDetail() {
        return bookingRepository.getBookingDetail();
    }

    public LiveData<List<RepairStatusEntity>> getStatusHistory() {
        return bookingRepository.getStatusHistory();
    }

    public LiveData<BookingRepository.OperationResult> getSubmitResult() {
        return bookingRepository.getSubmitResult();
    }

    public LiveData<BookingRepository.OperationResult> getUpdateStatusResult() {
        return bookingRepository.getUpdateStatusResult();
    }

    public void loadBookings(int userId) {
        bookingRepository.loadBookingsForUser(userId);
    }

    public void loadBookingDetail(int bookingId) {
        bookingRepository.loadBookingDetail(bookingId);
    }

    public void submitBooking(BookingEntity booking) {
        bookingRepository.submitBooking(booking);
    }

    public void advanceStatus(int bookingId) {
        bookingRepository.advanceStatus(bookingId);
    }
}
