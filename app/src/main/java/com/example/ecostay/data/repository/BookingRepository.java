package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.BookingDao;
import com.example.ecostay.data.dao.DeviceDao;
import com.example.ecostay.data.dao.RepairStatusDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.BookingEntity;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.data.entity.RepairStatusEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.data.model.AdminBookingDetail;
import com.example.ecostay.data.model.AdminBookingSummary;
import com.example.ecostay.data.model.BookingSummary;
import com.example.ecostay.data.model.DashboardStats;
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
    private final UserDao userDao;

    private final MutableLiveData<List<BookingSummary>> bookings = new MutableLiveData<>();
    private final MutableLiveData<BookingEntity> bookingDetail = new MutableLiveData<>();
    private final MutableLiveData<List<RepairStatusEntity>> statusHistory = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> submitResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> updateStatusResult = new MutableLiveData<>();

    private final MutableLiveData<List<AdminBookingSummary>> adminBookings = new MutableLiveData<>();
    private final MutableLiveData<AdminBookingDetail> adminBookingDetail = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> adminUpdateResult = new MutableLiveData<>();
    private final MutableLiveData<DashboardStats> dashboardStats = new MutableLiveData<>();

    public BookingRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        bookingDao = db.bookingDao();
        deviceDao = db.deviceDao();
        serviceDao = db.serviceDao();
        repairStatusDao = db.repairStatusDao();
        userDao = db.userDao();
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

    public LiveData<List<AdminBookingSummary>> getAdminBookings() {
        return adminBookings;
    }

    public LiveData<AdminBookingDetail> getAdminBookingDetail() {
        return adminBookingDetail;
    }

    public LiveData<OperationResult> getAdminUpdateResult() {
        return adminUpdateResult;
    }

    public LiveData<DashboardStats> getDashboardStats() {
        return dashboardStats;
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

    public void loadAllBookings(String statusFilter) {
        AppDatabase.getWriteExecutor().execute(() -> {
            List<BookingEntity> list;
            if (statusFilter == null || statusFilter.isEmpty() || "All".equals(statusFilter)) {
                list = bookingDao.getAll();
            } else {
                list = bookingDao.getByStatus(statusFilter);
            }
            List<AdminBookingSummary> summaries = new ArrayList<>();
            for (BookingEntity booking : list) {
                summaries.add(toAdminSummary(booking));
            }
            adminBookings.postValue(summaries);
        });
    }

    public void loadAdminBookingDetail(int bookingId) {
        AppDatabase.getWriteExecutor().execute(() -> {
            BookingEntity booking = bookingDao.getById(bookingId);
            if (booking == null) {
                adminBookingDetail.postValue(null);
                statusHistory.postValue(new ArrayList<>());
                return;
            }
            AdminBookingDetail detail = new AdminBookingDetail();
            detail.booking = booking;
            detail.customer = userDao.getById(booking.userId);
            detail.device = deviceDao.getById(booking.deviceId);
            detail.service = serviceDao.getById(booking.serviceId);
            adminBookingDetail.postValue(detail);
            statusHistory.postValue(repairStatusDao.getByBookingId(bookingId));
        });
    }

    public void loadDashboardStats() {
        AppDatabase.getWriteExecutor().execute(() -> {
            DashboardStats stats = new DashboardStats();
            stats.totalBookings = bookingDao.countAll();
            stats.receivedCount = bookingDao.countByStatus("Received");
            stats.technicianAssignedCount = bookingDao.countByStatus("Technician Assigned");
            stats.underRepairCount = bookingDao.countByStatus("Under Repair");
            stats.readyForPickupCount = bookingDao.countByStatus("Ready for Pickup");
            stats.completedCount = bookingDao.countByStatus("Completed");
            stats.cancelledCount = bookingDao.countByStatus("Cancelled");
            dashboardStats.postValue(stats);
        });
    }

    public void updateBookingAsAdmin(int bookingId, String status, String technicianName,
                                     String estimatedCompletion, String remarks, String updatedBy) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                BookingEntity booking = bookingDao.getById(bookingId);
                if (booking == null) {
                    adminUpdateResult.postValue(new OperationResult(false, "Booking not found", 0));
                    return;
                }
                String previousStatus = booking.status;
                booking.status = status;
                booking.technicianName = technicianName;
                if (estimatedCompletion != null && !estimatedCompletion.trim().isEmpty()) {
                    booking.estimatedCompletion = estimatedCompletion.trim();
                }
                if (remarks != null && !remarks.trim().isEmpty()) {
                    booking.adminRemarks = remarks.trim();
                }
                bookingDao.update(booking);

                RepairStatusEntity statusEntry = new RepairStatusEntity();
                statusEntry.bookingId = bookingId;
                statusEntry.status = status;
                statusEntry.remarks = remarks != null && !remarks.trim().isEmpty()
                        ? remarks.trim()
                        : "Status changed from " + previousStatus + " to " + status + ".";
                statusEntry.updatedAt = DateTimeUtils.nowIso();
                statusEntry.updatedBy = updatedBy != null && !updatedBy.isEmpty()
                        ? updatedBy
                        : "Admin";
                repairStatusDao.insert(statusEntry);

                adminUpdateResult.postValue(new OperationResult(true, "Booking updated", bookingId));
                loadAdminBookingDetail(bookingId);
            } catch (Exception e) {
                adminUpdateResult.postValue(new OperationResult(false, "Update failed", bookingId));
            }
        });
    }

    public int countBookingsForService(int serviceId) {
        return bookingDao.countByServiceId(serviceId);
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
                status.updatedBy = "System";
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
                status.updatedBy = "System";
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

    private AdminBookingSummary toAdminSummary(BookingEntity booking) {
        AdminBookingSummary summary = new AdminBookingSummary();
        summary.bookingId = booking.bookingId;
        summary.status = booking.status;
        summary.serviceMethod = booking.serviceMethod;
        summary.technicianName = booking.technicianName;
        summary.preferredDate = booking.preferredDate;
        summary.preferredTime = booking.preferredTime;
        summary.estimatedCompletion = booking.estimatedCompletion;

        UserEntity user = userDao.getById(booking.userId);
        summary.customerName = user != null ? user.fullName : "Unknown customer";

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
