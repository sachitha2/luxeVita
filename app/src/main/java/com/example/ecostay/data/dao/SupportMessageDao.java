package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ecostay.data.entity.SupportMessageEntity;

import java.util.List;

@Dao
public interface SupportMessageDao {

    @Insert
    long insert(SupportMessageEntity message);

    @Query("SELECT * FROM support_messages ORDER BY messageId DESC")
    List<SupportMessageEntity> getAll();
}
