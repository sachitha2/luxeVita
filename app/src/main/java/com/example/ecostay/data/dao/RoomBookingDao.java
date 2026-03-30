package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ecostay.data.entity.RoomBookingEntity;

import java.util.List;

@Dao
public interface RoomBookingDao {

    @Insert
    long insert(RoomBookingEntity booking);

    @Delete
    void delete(RoomBookingEntity booking);

    @Query("DELETE FROM room_bookings WHERE id = :bookingId")
    void deleteById(long bookingId);

    @Query(
            "SELECT COUNT(*) FROM room_bookings " +
                    "WHERE room_type_id = :roomTypeId " +
                    "AND status = 'CONFIRMED' " +
                    "AND NOT(:endDateEpochDay <= start_date_epoch_day OR :startDateEpochDay >= end_date_epoch_day)"
    )
    int countOverlappingConfirmed(
            long roomTypeId,
            long startDateEpochDay,
            long endDateEpochDay
    );

    @Query("SELECT * FROM room_bookings WHERE user_id = :userId AND status = 'CONFIRMED' ORDER BY created_at_epoch_millis DESC")
    List<RoomBookingEntity> getConfirmedByUser(long userId);

    @Query("SELECT * FROM room_bookings WHERE user_id = :userId ORDER BY created_at_epoch_millis DESC")
    List<RoomBookingEntity> getAllByUser(long userId);
}

