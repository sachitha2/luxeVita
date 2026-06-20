package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.data.repository.SlideshowRepository;
import com.example.ecostay.data.repository.SlideshowRepository.OperationResult;

import java.util.List;

public class AdminSlideshowViewModel extends AndroidViewModel {

    private final SlideshowRepository slideshowRepository;

    public AdminSlideshowViewModel(@NonNull Application application) {
        super(application);
        slideshowRepository = new SlideshowRepository(application);
    }

    public LiveData<List<SlideshowSlideEntity>> getSlides() {
        return slideshowRepository.getSlides();
    }

    public LiveData<OperationResult> getSaveResult() {
        return slideshowRepository.getSaveResult();
    }

    public LiveData<OperationResult> getDeleteResult() {
        return slideshowRepository.getDeleteResult();
    }

    public void loadSlides() {
        slideshowRepository.loadSlides();
    }

    public void loadSlideById(int slideId, MutableLiveData<SlideshowSlideEntity> liveData) {
        slideshowRepository.getSlideById(slideId, liveData);
    }

    public void saveSlide(SlideshowSlideEntity slide, boolean isEdit) {
        slideshowRepository.saveSlide(slide, isEdit);
    }

    public void deleteSlide(SlideshowSlideEntity slide) {
        slideshowRepository.deleteSlide(slide);
    }
}
