package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.DeviceDao;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.util.PhotoUtils;

import java.util.List;

public class DeviceRepository {

    public static final class OperationResult {
        public final boolean success;
        public final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private final DeviceDao deviceDao;
    private final MutableLiveData<List<DeviceEntity>> devices = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> saveResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> deleteResult = new MutableLiveData<>();

    public DeviceRepository(Application application) {
        deviceDao = AppDatabase.getInstance(application).deviceDao();
    }

    public LiveData<List<DeviceEntity>> getDevices() {
        return devices;
    }

    public LiveData<OperationResult> getSaveResult() {
        return saveResult;
    }

    public LiveData<OperationResult> getDeleteResult() {
        return deleteResult;
    }

    public void loadDevicesForUser(int userId) {
        AppDatabase.getWriteExecutor().execute(() ->
                devices.postValue(deviceDao.getByUserId(userId)));
    }

    public void saveDevice(DeviceEntity device, boolean isEdit) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                if (isEdit) {
                    deviceDao.update(device);
                } else {
                    deviceDao.insert(device);
                }
                saveResult.postValue(new OperationResult(true, "Device saved"));
                devices.postValue(deviceDao.getByUserId(device.userId));
            } catch (Exception e) {
                saveResult.postValue(new OperationResult(false, "Failed to save device"));
            }
        });
    }

    public void deleteDevice(DeviceEntity device) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                PhotoUtils.deletePhotoFile(device.imagePath);
                deviceDao.delete(device);
                deleteResult.postValue(new OperationResult(true, "Device deleted"));
                devices.postValue(deviceDao.getByUserId(device.userId));
            } catch (Exception e) {
                deleteResult.postValue(new OperationResult(false, "Failed to delete device"));
            }
        });
    }

    public void getDeviceById(int deviceId, MutableLiveData<DeviceEntity> liveData) {
        AppDatabase.getWriteExecutor().execute(() ->
                liveData.postValue(deviceDao.getById(deviceId)));
    }

    public void clearSaveResult() {
        saveResult.setValue(null);
    }
}
