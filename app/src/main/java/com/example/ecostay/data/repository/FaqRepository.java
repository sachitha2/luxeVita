package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.FaqDao;
import com.example.ecostay.data.entity.FaqEntity;

import java.util.List;

public class FaqRepository {

    private final FaqDao faqDao;
    private final MutableLiveData<List<FaqEntity>> faqs = new MutableLiveData<>();

    public FaqRepository(Application application) {
        faqDao = AppDatabase.getInstance(application).faqDao();
    }

    public LiveData<List<FaqEntity>> getFaqs() {
        return faqs;
    }

    public void loadFaqs() {
        AppDatabase.getWriteExecutor().execute(() ->
                faqs.postValue(faqDao.getAll()));
    }
}
