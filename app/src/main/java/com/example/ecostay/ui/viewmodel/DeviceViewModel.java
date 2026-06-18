package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.data.repository.DeviceRepository;

import java.util.List;

public class DeviceViewModel extends AndroidViewModel {

    private final DeviceRepository deviceRepository;

    public DeviceViewModel(@NonNull Application application) {
        super(application);
        deviceRepository = new DeviceRepository(application);
    }

    public LiveData<List<DeviceEntity>> getDevices() {
        return deviceRepository.getDevices();
    }

    public LiveData<DeviceRepository.OperationResult> getSaveResult() {
        return deviceRepository.getSaveResult();
    }

    public LiveData<DeviceRepository.OperationResult> getDeleteResult() {
        return deviceRepository.getDeleteResult();
    }

    public void loadDevices(int userId) {
        deviceRepository.loadDevicesForUser(userId);
    }

    public void saveDevice(DeviceEntity device, boolean isEdit) {
        deviceRepository.saveDevice(device, isEdit);
    }

    public void deleteDevice(DeviceEntity device) {
        deviceRepository.deleteDevice(device);
    }

    public void loadDevice(int deviceId, MutableLiveData<DeviceEntity> liveData) {
        deviceRepository.getDeviceById(deviceId, liveData);
    }
}
