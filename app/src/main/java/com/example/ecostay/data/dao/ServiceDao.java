package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.ServiceEntity;

import java.util.List;

@Dao
public interface ServiceDao {

    @Insert
    long insert(ServiceEntity service);

    @Update
    void update(ServiceEntity service);

    @Delete
    void delete(ServiceEntity service);

    @Query("SELECT * FROM services WHERE serviceId = :serviceId LIMIT 1")
    ServiceEntity getById(int serviceId);

    @Query("SELECT * FROM services ORDER BY deviceType, serviceName")
    List<ServiceEntity> getAll();

    @Query("SELECT * FROM services WHERE deviceType = :deviceType ORDER BY serviceName")
    List<ServiceEntity> getByDeviceType(String deviceType);
}
