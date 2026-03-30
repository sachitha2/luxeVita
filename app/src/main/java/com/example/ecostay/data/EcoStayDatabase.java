package com.example.ecostay.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ecostay.data.dao.AttractionDao;
import com.example.ecostay.data.dao.OfferDao;
import com.example.ecostay.data.dao.RoomBookingDao;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.dao.ServiceBookingDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.AttractionEntity;
import com.example.ecostay.data.entity.OfferEntity;
import com.example.ecostay.data.entity.RoomBookingEntity;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.data.entity.ServiceBookingEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.data.entity.UserEntity;

import java.time.LocalDate;

@Database(
        entities = {
                UserEntity.class,
                RoomTypeEntity.class,
                RoomBookingEntity.class,
                ServiceEntity.class,
                ServiceBookingEntity.class,
                OfferEntity.class,
                AttractionEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class EcoStayDatabase extends RoomDatabase {

    private static final String DB_NAME = "ecostay.db";

    public abstract UserDao userDao();

    public abstract RoomTypeDao roomTypeDao();

    public abstract RoomBookingDao roomBookingDao();

    public abstract ServiceDao serviceDao();

    public abstract ServiceBookingDao serviceBookingDao();

    public abstract OfferDao offerDao();

    public abstract AttractionDao attractionDao();

    private static volatile EcoStayDatabase INSTANCE;

    public static EcoStayDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (EcoStayDatabase.class) {
                if (INSTANCE == null) {
                    long todayEpochDay = LocalDate.now().toEpochDay();
                    long offersFrom = todayEpochDay - 7;
                    long offersTo = todayEpochDay + 30;

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    EcoStayDatabase.class,
                                    DB_NAME
                            )
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    // Seed sample data required by the assignment.
                                    // This runs only once when the database file is created.
                                    db.execSQL(
                                            "INSERT INTO room_types(name, description, price_per_night, totalRooms, imageRef) VALUES " +
                                                    "('Ocean View Suite','Luxury beachfront suite with a private balcony.',1200.0,10,'ocean_view_suite')," +
                                                    "('Deluxe Room','Comfortable deluxe room with elegant decor and sea breeze.',800.0,20,'deluxe_room')," +
                                                    "('Garden Suite','Quiet garden-facing suite for a relaxing stay.',900.0,15,'garden_suite')"
                                    );

                                    db.execSQL(
                                            "INSERT INTO services(name, category, description, price) VALUES " +
                                                    "('Spa Massage','SPA','60-minute relaxation massage.',150.0)," +
                                                    "('Fine Dining','DINING','Chef-curated multi-course dinner.',200.0)," +
                                                    "('Poolside Cabanas','CABANAS','Reserved cabana access with refreshments.',100.0)," +
                                                    "('Guided Beach Tour','TOURS','Local guide for an unforgettable beach experience.',180.0)"
                                    );

                                    db.execSQL(
                                            "INSERT INTO offers(title, description, valid_from_epoch_day, valid_to_epoch_day) VALUES " +
                                                    "('Stay 3 Nights, Pay 2','Enjoy a special rate when you stay longer.'," + offersFrom + "," + offersTo + ")," +
                                                    "('Free Breakfast','Complimentary breakfast for bookings this week.'," + (todayEpochDay - 1) + "," + (todayEpochDay + 7) + ")"
                                    );

                                    db.execSQL(
                                            "INSERT INTO attractions(title, description, location) VALUES " +
                                                    "('Sunset Beach Walk','Guided walk with ocean views.', 'LuxeVista Beach')," +
                                                    "('Water Sports Center','Try kayaking, paddleboarding and more.', 'Waterfront Area')," +
                                                    "('Local Seafood Market','Explore local flavors and fresh catches.', 'Old Town Marina')"
                                    );
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

