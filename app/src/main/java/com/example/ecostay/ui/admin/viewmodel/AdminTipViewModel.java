package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.data.repository.MaintenanceTipRepository;
import com.example.ecostay.data.repository.MaintenanceTipRepository.OperationResult;

import java.util.List;

public class AdminTipViewModel extends AndroidViewModel {

    private final MaintenanceTipRepository tipRepository;

    public AdminTipViewModel(@NonNull Application application) {
        super(application);
        tipRepository = new MaintenanceTipRepository(application);
    }

    public LiveData<List<MaintenanceTipEntity>> getTips() {
        return tipRepository.getTips();
    }

    public LiveData<OperationResult> getSaveResult() {
        return tipRepository.getSaveResult();
    }

    public LiveData<OperationResult> getDeleteResult() {
        return tipRepository.getDeleteResult();
    }

    public void loadTips() {
        tipRepository.loadTips();
    }

    public void loadTipsByDeviceType(String deviceType) {
        tipRepository.loadTipsByDeviceType(deviceType);
    }

    public void loadTipById(int tipId, MutableLiveData<MaintenanceTipEntity> liveData) {
        tipRepository.getTipById(tipId, liveData);
    }

    public void saveTip(MaintenanceTipEntity tip, boolean isEdit) {
        tipRepository.saveTip(tip, isEdit);
    }

    public void deleteTip(MaintenanceTipEntity tip) {
        tipRepository.deleteTip(tip);
    }
}
