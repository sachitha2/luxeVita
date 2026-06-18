package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.BookingDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.ServiceEntity;

import java.util.List;

public class ServiceRepository {

    public static final class OperationResult {
        public final boolean success;
        public final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private final ServiceDao serviceDao;
    private final BookingDao bookingDao;
    private final MutableLiveData<List<ServiceEntity>> services = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> saveResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> deleteResult = new MutableLiveData<>();

    public ServiceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        serviceDao = db.serviceDao();
        bookingDao = db.bookingDao();
    }

    public LiveData<List<ServiceEntity>> getServices() {
        return services;
    }

    public LiveData<OperationResult> getSaveResult() {
        return saveResult;
    }

    public LiveData<OperationResult> getDeleteResult() {
        return deleteResult;
    }

    public void loadAllServices() {
        AppDatabase.getWriteExecutor().execute(() ->
                services.postValue(serviceDao.getAll()));
    }

    public void loadServicesByDeviceType(String deviceType) {
        AppDatabase.getWriteExecutor().execute(() ->
                services.postValue(serviceDao.getByDeviceType(deviceType)));
    }

    public void getServiceById(int serviceId, MutableLiveData<ServiceEntity> liveData) {
        AppDatabase.getWriteExecutor().execute(() ->
                liveData.postValue(serviceDao.getById(serviceId)));
    }

    public void saveService(ServiceEntity service, boolean isEdit) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                if (isEdit) {
                    serviceDao.update(service);
                } else {
                    serviceDao.insert(service);
                }
                saveResult.postValue(new OperationResult(true, "Service saved"));
                services.postValue(serviceDao.getAll());
            } catch (Exception e) {
                saveResult.postValue(new OperationResult(false, "Failed to save service"));
            }
        });
    }

    public void deleteService(ServiceEntity service) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                int count = bookingDao.countByServiceId(service.serviceId);
                if (count > 0) {
                    deleteResult.postValue(new OperationResult(false, "has_bookings"));
                    return;
                }
                serviceDao.delete(service);
                deleteResult.postValue(new OperationResult(true, "Service deleted"));
                services.postValue(serviceDao.getAll());
            } catch (Exception e) {
                deleteResult.postValue(new OperationResult(false, "Failed to delete service"));
            }
        });
    }
}
