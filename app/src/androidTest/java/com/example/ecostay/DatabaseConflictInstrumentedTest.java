package com.example.ecostay;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.RoomBookingDao;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.dao.ServiceBookingDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.RoomBookingEntity;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.data.entity.ServiceBookingEntity;
import com.example.ecostay.data.entity.ServiceEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DatabaseConflictInstrumentedTest {

    private EcoStayDatabase db;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        EcoStayDatabase.class
                )
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void tearDown() {
        if (db != null) db.close();
    }

    @Test
    public void roomBooking_overlapConflictCount_isCorrect() {
        RoomTypeDao roomTypeDao = db.roomTypeDao();
        RoomBookingDao roomBookingDao = db.roomBookingDao();

        RoomTypeEntity rt = new RoomTypeEntity();
        rt.name = "Test Room";
        rt.description = "desc";
        rt.pricePerNight = 100.0;
        rt.totalRooms = 2;
        rt.imageRef = "";

        long[] rtIds = roomTypeDao.insertAll(rt);
        long roomTypeId = rtIds[0];

        RoomBookingEntity b1 = new RoomBookingEntity();
        b1.userId = 1;
        b1.roomTypeId = roomTypeId;
        b1.startDateEpochDay = 10;
        b1.endDateEpochDay = 13;
        b1.status = "CONFIRMED";
        b1.createdAtEpochMillis = 0;
        roomBookingDao.insert(b1);

        RoomBookingEntity b2 = new RoomBookingEntity();
        b2.userId = 2;
        b2.roomTypeId = roomTypeId;
        b2.startDateEpochDay = 12;
        b2.endDateEpochDay = 14;
        b2.status = "CONFIRMED";
        b2.createdAtEpochMillis = 0;
        roomBookingDao.insert(b2);

        // Query range [11,12) overlaps only with b1 (10-13). It should NOT overlap b2 (12-14).
        int overlapCount = roomBookingDao.countOverlappingConfirmed(roomTypeId, 11, 12);
        assertEquals(1, overlapCount);
    }

    @Test
    public void serviceBooking_conflictCount_isCorrect() {
        ServiceDao serviceDao = db.serviceDao();
        ServiceBookingDao serviceBookingDao = db.serviceBookingDao();

        ServiceEntity s = new ServiceEntity();
        s.name = "Test Spa";
        s.category = "SPA";
        s.description = "desc";
        s.price = 50.0;

        long[] serviceIds = serviceDao.insertAll(s);
        long serviceId = serviceIds[0];

        ServiceBookingEntity booking = new ServiceBookingEntity();
        booking.userId = 1;
        booking.serviceId = serviceId;
        booking.bookingDateEpochDay = 100;
        booking.status = "CONFIRMED";
        booking.createdAtEpochMillis = 0;
        serviceBookingDao.insert(booking);

        int count = serviceBookingDao.countConfirmedForDate(serviceId, 100);
        assertEquals(1, count);
    }
}

