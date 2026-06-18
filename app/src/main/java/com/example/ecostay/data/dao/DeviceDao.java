package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.DeviceEntity;

import java.util.List;

@Dao
public interface DeviceDao {

    @Insert
    long insert(DeviceEntity device);

    @Update
    void update(DeviceEntity device);

    @Delete
    void delete(DeviceEntity device);

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    DeviceEntity getById(int deviceId);

    @Query("SELECT * FROM devices")
    List<DeviceEntity> getAll();

    @Query("SELECT * FROM devices WHERE userId = :userId ORDER BY deviceId DESC")
    List<DeviceEntity> getByUserId(int userId);
}
