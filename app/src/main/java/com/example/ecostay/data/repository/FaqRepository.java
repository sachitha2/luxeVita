package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.FaqDao;
import com.example.ecostay.data.entity.FaqEntity;

import java.util.List;

public class FaqRepository {

    public static final class OperationResult {
        public final boolean success;
        public final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private final FaqDao faqDao;
    private final MutableLiveData<List<FaqEntity>> faqs = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> saveResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> deleteResult = new MutableLiveData<>();

    public FaqRepository(Application application) {
        faqDao = AppDatabase.getInstance(application).faqDao();
    }

    public LiveData<List<FaqEntity>> getFaqs() {
        return faqs;
    }

    public LiveData<OperationResult> getSaveResult() {
        return saveResult;
    }

    public LiveData<OperationResult> getDeleteResult() {
        return deleteResult;
    }

    public void loadFaqs() {
        AppDatabase.getWriteExecutor().execute(() ->
                faqs.postValue(faqDao.getAll()));
    }

    public void getFaqById(int faqId, MutableLiveData<FaqEntity> liveData) {
        AppDatabase.getWriteExecutor().execute(() ->
                liveData.postValue(faqDao.getById(faqId)));
    }

    public void saveFaq(FaqEntity faq, boolean isEdit) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                if (isEdit) {
                    faqDao.update(faq);
                } else {
                    faqDao.insert(faq);
                }
                saveResult.postValue(new OperationResult(true, "FAQ saved"));
                faqs.postValue(faqDao.getAll());
            } catch (Exception e) {
                saveResult.postValue(new OperationResult(false, "Failed to save FAQ"));
            }
        });
    }

    public void deleteFaq(FaqEntity faq) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                faqDao.delete(faq);
                deleteResult.postValue(new OperationResult(true, "FAQ deleted"));
                faqs.postValue(faqDao.getAll());
            } catch (Exception e) {
                deleteResult.postValue(new OperationResult(false, "Failed to delete FAQ"));
            }
        });
    }
}
