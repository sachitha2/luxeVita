package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.RoomTypeEntity;

import java.util.List;

@Dao
public interface RoomTypeDao {

    @Query("SELECT * FROM room_types ORDER BY price_per_night ASC")
    List<RoomTypeEntity> getAll();

    @Insert
    long[] insertAll(RoomTypeEntity... roomTypes);

    @Insert
    long insert(RoomTypeEntity roomType);

    @Update
    void update(RoomTypeEntity roomType);

    @Delete
    void delete(RoomTypeEntity roomType);

    @Query("SELECT * FROM room_types WHERE id = :id LIMIT 1")
    RoomTypeEntity findById(long id);
}

