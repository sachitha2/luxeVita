package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.entity.FaqEntity;
import com.example.ecostay.data.repository.FaqRepository;
import com.example.ecostay.data.repository.FaqRepository.OperationResult;

import java.util.List;

public class AdminFaqViewModel extends AndroidViewModel {

    private final FaqRepository faqRepository;

    public AdminFaqViewModel(@NonNull Application application) {
        super(application);
        faqRepository = new FaqRepository(application);
    }

    public LiveData<List<FaqEntity>> getFaqs() {
        return faqRepository.getFaqs();
    }

    public LiveData<OperationResult> getSaveResult() {
        return faqRepository.getSaveResult();
    }

    public LiveData<OperationResult> getDeleteResult() {
        return faqRepository.getDeleteResult();
    }

    public void loadFaqs() {
        faqRepository.loadFaqs();
    }

    public void loadFaqById(int faqId, MutableLiveData<FaqEntity> liveData) {
        faqRepository.getFaqById(faqId, liveData);
    }

    public void saveFaq(FaqEntity faq, boolean isEdit) {
        faqRepository.saveFaq(faq, isEdit);
    }

    public void deleteFaq(FaqEntity faq) {
        faqRepository.deleteFaq(faq);
    }
}
