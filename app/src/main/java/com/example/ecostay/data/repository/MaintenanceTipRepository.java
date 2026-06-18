package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.MaintenanceTipDao;
import com.example.ecostay.data.entity.MaintenanceTipEntity;

import java.util.List;

public class MaintenanceTipRepository {

    public static final class OperationResult {
        public final boolean success;
        public final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private final MaintenanceTipDao tipDao;
    private final MutableLiveData<List<MaintenanceTipEntity>> tips = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> saveResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> deleteResult = new MutableLiveData<>();

    public MaintenanceTipRepository(Application application) {
        tipDao = AppDatabase.getInstance(application).maintenanceTipDao();
    }

    public LiveData<List<MaintenanceTipEntity>> getTips() {
        return tips;
    }

    public LiveData<OperationResult> getSaveResult() {
        return saveResult;
    }

    public LiveData<OperationResult> getDeleteResult() {
        return deleteResult;
    }

    public void loadTips() {
        AppDatabase.getWriteExecutor().execute(() ->
                tips.postValue(tipDao.getAll()));
    }

    public void loadTipsByDeviceType(String deviceType) {
        AppDatabase.getWriteExecutor().execute(() -> {
            if (deviceType == null || deviceType.isEmpty() || "All".equals(deviceType)) {
                tips.postValue(tipDao.getAll());
            } else {
                tips.postValue(tipDao.getByDeviceType(deviceType));
            }
        });
    }

    public void getTipById(int tipId, MutableLiveData<MaintenanceTipEntity> liveData) {
        AppDatabase.getWriteExecutor().execute(() ->
                liveData.postValue(tipDao.getById(tipId)));
    }

    public void saveTip(MaintenanceTipEntity tip, boolean isEdit) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                if (isEdit) {
                    tipDao.update(tip);
                } else {
                    tipDao.insert(tip);
                }
                saveResult.postValue(new OperationResult(true, "Tip saved"));
                tips.postValue(tipDao.getAll());
            } catch (Exception e) {
                saveResult.postValue(new OperationResult(false, "Failed to save tip"));
            }
        });
    }

    public void deleteTip(MaintenanceTipEntity tip) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                tipDao.delete(tip);
                deleteResult.postValue(new OperationResult(true, "Tip deleted"));
                tips.postValue(tipDao.getAll());
            } catch (Exception e) {
                deleteResult.postValue(new OperationResult(false, "Failed to delete tip"));
            }
        });
    }
}
