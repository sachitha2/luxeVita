package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.ServiceBookingEntity;

import java.util.List;

@Dao
public interface ServiceBookingDao {

    @Insert
    long insert(ServiceBookingEntity booking);

    @Update
    void update(ServiceBookingEntity booking);

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

    @Query("SELECT * FROM service_bookings WHERE id = :bookingId AND user_id = :userId LIMIT 1")
    ServiceBookingEntity findByIdForUser(long bookingId, long userId);

    @Query(
            "SELECT * FROM service_bookings " +
                    "WHERE user_id = :userId " +
                    "AND (:status = 'ALL' OR status = :status) " +
                    "ORDER BY " +
                    "CASE WHEN :sortBy = 'created_asc' THEN created_at_epoch_millis END ASC, " +
                    "CASE WHEN :sortBy = 'date_asc' THEN booking_date_epoch_day END ASC, " +
                    "CASE WHEN :sortBy = 'date_desc' THEN booking_date_epoch_day END DESC, " +
                    "created_at_epoch_millis DESC"
    )
    List<ServiceBookingEntity> getManagedByUser(long userId, String status, String sortBy);
}

