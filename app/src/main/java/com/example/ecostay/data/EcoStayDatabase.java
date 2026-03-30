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
                                    seedRoomsAndServicesIfMissing(db);
                                    seedOffersAndAttractions(db, todayEpochDay, offersFrom, offersTo);
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    // Backfill core catalog data safely for existing databases.
                                    // Rows are inserted only when missing.
                                    seedRoomsAndServicesIfMissing(db);
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedRoomsAndServicesIfMissing(@NonNull SupportSQLiteDatabase db) {
        db.execSQL(
                "INSERT INTO room_types(name, description, price_per_night, totalRooms, imageRef) " +
                        "SELECT 'Ocean View Suite','Luxury beachfront suite with a private balcony.',1200.0,10,'ocean_view_suite' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM room_types WHERE name = 'Ocean View Suite')"
        );
        db.execSQL(
                "INSERT INTO room_types(name, description, price_per_night, totalRooms, imageRef) " +
                        "SELECT 'Deluxe Room','Comfortable deluxe room with elegant decor and sea breeze.',800.0,20,'deluxe_room' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM room_types WHERE name = 'Deluxe Room')"
        );
        db.execSQL(
                "INSERT INTO room_types(name, description, price_per_night, totalRooms, imageRef) " +
                        "SELECT 'Garden Suite','Quiet garden-facing suite for a relaxing stay.',900.0,15,'garden_suite' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM room_types WHERE name = 'Garden Suite')"
        );

        db.execSQL(
                "INSERT INTO services(name, category, description, price) " +
                        "SELECT 'Spa Massage','SPA','60-minute relaxation massage.',150.0 " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Spa Massage')"
        );
        db.execSQL(
                "INSERT INTO services(name, category, description, price) " +
                        "SELECT 'Fine Dining','DINING','Chef-curated multi-course dinner.',200.0 " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Fine Dining')"
        );
        db.execSQL(
                "INSERT INTO services(name, category, description, price) " +
                        "SELECT 'Poolside Cabanas','CABANAS','Reserved cabana access with refreshments.',100.0 " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Poolside Cabanas')"
        );
        db.execSQL(
                "INSERT INTO services(name, category, description, price) " +
                        "SELECT 'Guided Beach Tour','TOURS','Local guide for an unforgettable beach experience.',180.0 " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Guided Beach Tour')"
        );
    }

    private static void seedOffersAndAttractions(
            @NonNull SupportSQLiteDatabase db,
            long todayEpochDay,
            long offersFrom,
            long offersTo
    ) {
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
}

