package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.data.repository.ServiceRepository;
import com.example.ecostay.data.repository.ServiceRepository.OperationResult;

import java.util.List;

public class AdminServiceViewModel extends AndroidViewModel {

    private final ServiceRepository serviceRepository;

    public AdminServiceViewModel(@NonNull Application application) {
        super(application);
        serviceRepository = new ServiceRepository(application);
    }

    public LiveData<List<ServiceEntity>> getServices() {
        return serviceRepository.getServices();
    }

    public LiveData<OperationResult> getSaveResult() {
        return serviceRepository.getSaveResult();
    }

    public LiveData<OperationResult> getDeleteResult() {
        return serviceRepository.getDeleteResult();
    }

    public void loadServices() {
        serviceRepository.loadAllServices();
    }

    public void loadServiceById(int serviceId, MutableLiveData<ServiceEntity> liveData) {
        serviceRepository.getServiceById(serviceId, liveData);
    }

    public void saveService(ServiceEntity service, boolean isEdit) {
        serviceRepository.saveService(service, isEdit);
    }

    public void deleteService(ServiceEntity service) {
        serviceRepository.deleteService(service);
    }
}
