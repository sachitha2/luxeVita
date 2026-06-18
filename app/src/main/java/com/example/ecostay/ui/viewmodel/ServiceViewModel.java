package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.data.repository.ServiceRepository;

import java.util.List;

public class ServiceViewModel extends AndroidViewModel {

    private final ServiceRepository serviceRepository;
    private final MutableLiveData<String> selectedDeviceType = new MutableLiveData<>();

    public ServiceViewModel(@NonNull Application application) {
        super(application);
        serviceRepository = new ServiceRepository(application);
    }

    public LiveData<List<ServiceEntity>> getServices() {
        return serviceRepository.getServices();
    }

    public LiveData<String> getSelectedDeviceType() {
        return selectedDeviceType;
    }

    public void loadAllServices() {
        serviceRepository.loadAllServices();
    }

    public void loadServicesByDeviceType(String deviceType) {
        selectedDeviceType.setValue(deviceType);
        serviceRepository.loadServicesByDeviceType(deviceType);
    }

    public void loadService(int serviceId, MutableLiveData<ServiceEntity> liveData) {
        serviceRepository.getServiceById(serviceId, liveData);
    }
}
