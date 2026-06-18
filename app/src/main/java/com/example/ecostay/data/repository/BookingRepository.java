package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.BookingDao;
import com.example.ecostay.data.dao.DeviceDao;
import com.example.ecostay.data.dao.RepairStatusDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.BookingEntity;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.data.entity.RepairStatusEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.data.model.BookingSummary;
import com.example.ecostay.util.DateTimeUtils;
import com.example.ecostay.util.TechnicianUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingRepository {

    public static final class OperationResult {
        public final boolean success;
        public final String message;
        public final int bookingId;

        public OperationResult(boolean success, String message, int bookingId) {
            this.success = success;
            this.message = message;
            this.bookingId = bookingId;
        }
    }

    private final BookingDao bookingDao;
    private final DeviceDao deviceDao;
    private final ServiceDao serviceDao;
    private final RepairStatusDao repairStatusDao;

    private final MutableLiveData<List<BookingSummary>> bookings = new MutableLiveData<>();
    private final MutableLiveData<BookingEntity> bookingDetail = new MutableLiveData<>();
    private final MutableLiveData<List<RepairStatusEntity>> statusHistory = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> submitResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> updateStatusResult = new MutableLiveData<>();

    public BookingRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        bookingDao = db.bookingDao();
        deviceDao = db.deviceDao();
        serviceDao = db.serviceDao();
        repairStatusDao = db.repairStatusDao();
    }

    public LiveData<List<BookingSummary>> getBookings() {
        return bookings;
    }

    public LiveData<BookingEntity> getBookingDetail() {
        return bookingDetail;
    }

    public LiveData<List<RepairStatusEntity>> getStatusHistory() {
        return statusHistory;
    }

    public LiveData<OperationResult> getSubmitResult() {
        return submitResult;
    }

    public LiveData<OperationResult> getUpdateStatusResult() {
        return updateStatusResult;
    }

    public void loadBookingsForUser(int userId) {
        AppDatabase.getWriteExecutor().execute(() -> {
            List<BookingEntity> list = bookingDao.getByUserId(userId);
            List<BookingSummary> summaries = new ArrayList<>();
            for (BookingEntity booking : list) {
                summaries.add(toSummary(booking));
            }
            bookings.postValue(summaries);
        });
    }

    public void loadBookingDetail(int bookingId) {
        AppDatabase.getWriteExecutor().execute(() -> {
            bookingDetail.postValue(bookingDao.getById(bookingId));
            statusHistory.postValue(repairStatusDao.getByBookingId(bookingId));
        });
    }

    public void submitBooking(BookingEntity booking) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                DeviceEntity device = deviceDao.getById(booking.deviceId);
                if (device == null) {
                    submitResult.postValue(new OperationResult(false, "Device not found", 0));
                    return;
                }
                booking.technicianName = TechnicianUtils.getTechnicianForDeviceType(device.deviceType);
                booking.status = "Received";
                booking.estimatedCompletion = DateTimeUtils.addDaysToDate(booking.preferredDate, 2);
                booking.createdAt = DateTimeUtils.nowIso();

                long id = bookingDao.insert(booking);
                int bookingId = (int) id;

                RepairStatusEntity status = new RepairStatusEntity();
                status.bookingId = bookingId;
                status.status = "Received";
                status.remarks = "Repair request received and logged.";
                status.updatedAt = DateTimeUtils.nowIso();
                repairStatusDao.insert(status);

                submitResult.postValue(new OperationResult(true, "Repair request submitted", bookingId));
            } catch (Exception e) {
                submitResult.postValue(new OperationResult(false, "Failed to submit request", 0));
            }
        });
    }

    public void advanceStatus(int bookingId) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                BookingEntity booking = bookingDao.getById(bookingId);
                if (booking == null) {
                    updateStatusResult.postValue(new OperationResult(false, "Booking not found", 0));
                    return;
                }
                if (!TechnicianUtils.canAdvanceStatus(booking.status)) {
                    updateStatusResult.postValue(new OperationResult(false, "Already completed", bookingId));
                    return;
                }
                String nextStatus = TechnicianUtils.getNextStatus(booking.status);
                booking.status = nextStatus;
                bookingDao.update(booking);

                RepairStatusEntity status = new RepairStatusEntity();
                status.bookingId = bookingId;
                status.status = nextStatus;
                status.remarks = "Status updated to " + nextStatus + ".";
                status.updatedAt = DateTimeUtils.nowIso();
                repairStatusDao.insert(status);

                updateStatusResult.postValue(new OperationResult(true, "Status updated", bookingId));
                bookingDetail.postValue(booking);
                statusHistory.postValue(repairStatusDao.getByBookingId(bookingId));
            } catch (Exception e) {
                updateStatusResult.postValue(new OperationResult(false, "Update failed", bookingId));
            }
        });
    }

    private BookingSummary toSummary(BookingEntity booking) {
        BookingSummary summary = new BookingSummary();
        summary.bookingId = booking.bookingId;
        summary.status = booking.status;
        summary.serviceMethod = booking.serviceMethod;
        summary.technicianName = booking.technicianName;
        summary.estimatedCompletion = booking.estimatedCompletion;

        ServiceEntity service = serviceDao.getById(booking.serviceId);
        summary.serviceName = service != null ? service.serviceName : "Unknown service";

        DeviceEntity device = deviceDao.getById(booking.deviceId);
        if (device != null) {
            summary.deviceLabel = device.brand + " " + device.model + " (" + device.deviceType + ")";
        } else {
            summary.deviceLabel = "Unknown device";
        }
        return summary;
    }
}
