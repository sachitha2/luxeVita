package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.SlideshowSlideEntity;

import java.util.List;

@Dao
public interface SlideshowDao {

    @Insert
    long insert(SlideshowSlideEntity slide);

    @Update
    void update(SlideshowSlideEntity slide);

    @Delete
    void delete(SlideshowSlideEntity slide);

    @Query("SELECT * FROM slideshow_slides WHERE slideId = :slideId LIMIT 1")
    SlideshowSlideEntity getById(int slideId);

    @Query("SELECT * FROM slideshow_slides ORDER BY sort_order ASC, slideId ASC")
    List<SlideshowSlideEntity> getAll();

    @Query("SELECT * FROM slideshow_slides WHERE is_active = 1 ORDER BY sort_order ASC, slideId ASC")
    List<SlideshowSlideEntity> getActiveSlides();

    @Query("SELECT COALESCE(MAX(sort_order), 0) FROM slideshow_slides")
    int getMaxSortOrder();
}
