package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ecostay.data.entity.AttractionEntity;

import java.util.List;

@Dao
public interface AttractionDao {

    @Query("SELECT * FROM attractions ORDER BY title ASC")
    List<AttractionEntity> getAll();

    @Insert
    long[] insertAll(AttractionEntity... attractions);
}

