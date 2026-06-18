package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.RepairStatusEntity;

import java.util.List;

@Dao
public interface RepairStatusDao {

    @Insert
    long insert(RepairStatusEntity status);

    @Update
    void update(RepairStatusEntity status);

    @Delete
    void delete(RepairStatusEntity status);

    @Query("SELECT * FROM repair_status WHERE statusId = :statusId LIMIT 1")
    RepairStatusEntity getById(int statusId);

    @Query("SELECT * FROM repair_status")
    List<RepairStatusEntity> getAll();

    @Query("SELECT * FROM repair_status WHERE bookingId = :bookingId ORDER BY statusId ASC")
    List<RepairStatusEntity> getByBookingId(int bookingId);
}
