package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.SlideshowDao;
import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.util.PhotoUtils;

import java.util.List;

public class SlideshowRepository {

    public static final class OperationResult {
        public final boolean success;
        public final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private final SlideshowDao slideshowDao;
    private final MutableLiveData<List<SlideshowSlideEntity>> slides = new MutableLiveData<>();
    private final MutableLiveData<List<SlideshowSlideEntity>> activeSlides = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> saveResult = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> deleteResult = new MutableLiveData<>();

    public SlideshowRepository(Application application) {
        slideshowDao = AppDatabase.getInstance(application).slideshowDao();
    }

    public LiveData<List<SlideshowSlideEntity>> getSlides() {
        return slides;
    }

    public LiveData<List<SlideshowSlideEntity>> getActiveSlides() {
        return activeSlides;
    }

    public LiveData<OperationResult> getSaveResult() {
        return saveResult;
    }

    public LiveData<OperationResult> getDeleteResult() {
        return deleteResult;
    }

    public void loadSlides() {
        AppDatabase.getWriteExecutor().execute(() ->
                slides.postValue(slideshowDao.getAll()));
    }

    public void loadActiveSlides() {
        AppDatabase.getWriteExecutor().execute(() ->
                activeSlides.postValue(slideshowDao.getActiveSlides()));
    }

    public void getSlideById(int slideId, MutableLiveData<SlideshowSlideEntity> liveData) {
        AppDatabase.getWriteExecutor().execute(() ->
                liveData.postValue(slideshowDao.getById(slideId)));
    }

    public void saveSlide(SlideshowSlideEntity slide, boolean isEdit) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                if (isEdit) {
                    slideshowDao.update(slide);
                } else {
                    if (slide.sortOrder <= 0) {
                        slide.sortOrder = slideshowDao.getMaxSortOrder() + 1;
                    }
                    slideshowDao.insert(slide);
                }
                saveResult.postValue(new OperationResult(true, "Slide saved"));
                slides.postValue(slideshowDao.getAll());
                activeSlides.postValue(slideshowDao.getActiveSlides());
            } catch (Exception e) {
                saveResult.postValue(new OperationResult(false, "Failed to save slide"));
            }
        });
    }

    public void deleteSlide(SlideshowSlideEntity slide) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                PhotoUtils.deletePhotoFile(slide.imagePath);
                slideshowDao.delete(slide);
                deleteResult.postValue(new OperationResult(true, "Slide deleted"));
                slides.postValue(slideshowDao.getAll());
                activeSlides.postValue(slideshowDao.getActiveSlides());
            } catch (Exception e) {
                deleteResult.postValue(new OperationResult(false, "Failed to delete slide"));
            }
        });
    }
}
