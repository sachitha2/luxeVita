package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.ServiceEntity;

import java.util.List;

public class ServiceRepository {

    private final ServiceDao serviceDao;
    private final MutableLiveData<List<ServiceEntity>> services = new MutableLiveData<>();

    public ServiceRepository(Application application) {
        serviceDao = AppDatabase.getInstance(application).serviceDao();
    }

    public LiveData<List<ServiceEntity>> getServices() {
        return services;
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
}
