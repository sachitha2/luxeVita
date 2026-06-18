package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.MaintenanceTipEntity;

import java.util.List;

@Dao
public interface MaintenanceTipDao {

    @Insert
    long insert(MaintenanceTipEntity tip);

    @Update
    void update(MaintenanceTipEntity tip);

    @Delete
    void delete(MaintenanceTipEntity tip);

    @Query("SELECT * FROM maintenance_tips WHERE tipId = :tipId LIMIT 1")
    MaintenanceTipEntity getById(int tipId);

    @Query("SELECT * FROM maintenance_tips ORDER BY deviceType, tipId")
    List<MaintenanceTipEntity> getAll();

    @Query("SELECT * FROM maintenance_tips WHERE deviceType = :deviceType ORDER BY tipId")
    List<MaintenanceTipEntity> getByDeviceType(String deviceType);
}
