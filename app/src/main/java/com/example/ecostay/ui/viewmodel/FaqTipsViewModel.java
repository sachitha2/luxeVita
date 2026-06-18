package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.entity.FaqEntity;
import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.data.repository.FaqRepository;
import com.example.ecostay.data.repository.MaintenanceTipRepository;

import java.util.List;

public class FaqTipsViewModel extends AndroidViewModel {

    private final FaqRepository faqRepository;
    private final MaintenanceTipRepository tipRepository;

    public FaqTipsViewModel(@NonNull Application application) {
        super(application);
        faqRepository = new FaqRepository(application);
        tipRepository = new MaintenanceTipRepository(application);
    }

    public LiveData<List<FaqEntity>> getFaqs() {
        return faqRepository.getFaqs();
    }

    public LiveData<List<MaintenanceTipEntity>> getTips() {
        return tipRepository.getTips();
    }

    public void loadContent() {
        faqRepository.loadFaqs();
        tipRepository.loadTips();
    }
}
