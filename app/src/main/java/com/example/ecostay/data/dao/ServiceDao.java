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

    @Query("SELECT * FROM services ORDER BY name ASC")
    List<ServiceEntity> getAll();

    @Query("SELECT * FROM services WHERE category = :category ORDER BY name ASC")
    List<ServiceEntity> getByCategory(String category);

    @Insert
    long[] insertAll(ServiceEntity... services);

    @Insert
    long insert(ServiceEntity service);

    @Update
    void update(ServiceEntity service);

    @Delete
    void delete(ServiceEntity service);

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    ServiceEntity findById(long id);
}

