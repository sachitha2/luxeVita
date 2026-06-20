package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.data.repository.SlideshowRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final SlideshowRepository slideshowRepository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        slideshowRepository = new SlideshowRepository(application);
    }

    public LiveData<List<SlideshowSlideEntity>> getActiveSlides() {
        return slideshowRepository.getActiveSlides();
    }

    public void loadActiveSlides() {
        slideshowRepository.loadActiveSlides();
    }
}
