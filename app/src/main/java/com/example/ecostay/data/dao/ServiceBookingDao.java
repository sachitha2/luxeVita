package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ecostay.data.entity.ServiceBookingEntity;

import java.util.List;

@Dao
public interface ServiceBookingDao {

    @Insert
    long insert(ServiceBookingEntity booking);

    @Delete
    void delete(ServiceBookingEntity booking);

    @Query("DELETE FROM service_bookings WHERE id = :bookingId")
    void deleteById(long bookingId);

    @Query(
            "SELECT COUNT(*) FROM service_bookings " +
                    "WHERE service_id = :serviceId " +
                    "AND status = 'CONFIRMED' " +
                    "AND booking_date_epoch_day = :dateEpochDay"
    )
    int countConfirmedForDate(long serviceId, long dateEpochDay);

    @Query("SELECT * FROM service_bookings WHERE user_id = :userId AND status = 'CONFIRMED' ORDER BY created_at_epoch_millis DESC")
    List<ServiceBookingEntity> getConfirmedByUser(long userId);

    @Query(
            "SELECT * FROM service_bookings " +
                    "WHERE user_id = :userId " +
                    "AND service_id = :serviceId " +
                    "AND booking_date_epoch_day = :dateEpochDay " +
                    "AND status = 'CONFIRMED' " +
                    "LIMIT 1"
    )
    ServiceBookingEntity findConfirmedByUserServiceDate(long userId, long serviceId, long dateEpochDay);

    @Query(
            "DELETE FROM service_bookings " +
                    "WHERE user_id = :userId " +
                    "AND service_id = :serviceId " +
                    "AND booking_date_epoch_day = :dateEpochDay " +
                    "AND status = 'CONFIRMED'"
    )
    void deleteConfirmedByUserServiceDate(long userId, long serviceId, long dateEpochDay);

    @Query("SELECT * FROM service_bookings WHERE user_id = :userId ORDER BY created_at_epoch_millis DESC")
    List<ServiceBookingEntity> getAllByUser(long userId);
}

