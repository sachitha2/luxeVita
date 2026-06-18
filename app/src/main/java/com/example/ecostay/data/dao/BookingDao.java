package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.BookingEntity;

import java.util.List;

@Dao
public interface BookingDao {

    @Insert
    long insert(BookingEntity booking);

    @Update
    void update(BookingEntity booking);

    @Delete
    void delete(BookingEntity booking);

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId LIMIT 1")
    BookingEntity getById(int bookingId);

    @Query("SELECT * FROM bookings ORDER BY bookingId DESC")
    List<BookingEntity> getAll();

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY bookingId DESC")
    List<BookingEntity> getByUserId(int userId);

    @Query("SELECT * FROM bookings WHERE status = :status ORDER BY bookingId DESC")
    List<BookingEntity> getByStatus(String status);

    @Query("SELECT COUNT(*) FROM bookings")
    int countAll();

    @Query("SELECT COUNT(*) FROM bookings WHERE status = :status")
    int countByStatus(String status);

    @Query("SELECT COUNT(*) FROM bookings WHERE serviceId = :serviceId")
    int countByServiceId(int serviceId);
}
