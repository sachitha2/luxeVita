package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.MaintenanceTipDao;
import com.example.ecostay.data.entity.MaintenanceTipEntity;

import java.util.List;

public class MaintenanceTipRepository {

    private final MaintenanceTipDao tipDao;
    private final MutableLiveData<List<MaintenanceTipEntity>> tips = new MutableLiveData<>();

    public MaintenanceTipRepository(Application application) {
        tipDao = AppDatabase.getInstance(application).maintenanceTipDao();
    }

    public LiveData<List<MaintenanceTipEntity>> getTips() {
        return tips;
    }

    public void loadTips() {
        AppDatabase.getWriteExecutor().execute(() ->
                tips.postValue(tipDao.getAll()));
    }
}
