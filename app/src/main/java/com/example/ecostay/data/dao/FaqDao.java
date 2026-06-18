package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.FaqEntity;

import java.util.List;

@Dao
public interface FaqDao {

    @Insert
    long insert(FaqEntity faq);

    @Update
    void update(FaqEntity faq);

    @Delete
    void delete(FaqEntity faq);

    @Query("SELECT * FROM faqs WHERE faqId = :faqId LIMIT 1")
    FaqEntity getById(int faqId);

    @Query("SELECT * FROM faqs ORDER BY faqId")
    List<FaqEntity> getAll();
}
