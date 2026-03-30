package com.example.ecostay.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
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
        version = 3,
        exportSchema = false
)
public abstract class EcoStayDatabase extends RoomDatabase {

    private static final String DB_NAME = "ecostay.db";
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE room_bookings ADD COLUMN payment_status TEXT NOT NULL DEFAULT 'PENDING'");
            db.execSQL("ALTER TABLE room_bookings ADD COLUMN payment_method TEXT");
            db.execSQL("ALTER TABLE room_bookings ADD COLUMN total_amount REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE room_bookings ADD COLUMN updated_at_epoch_millis INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE room_bookings ADD COLUMN cancelled_at_epoch_millis INTEGER");

            db.execSQL("ALTER TABLE service_bookings ADD COLUMN payment_status TEXT NOT NULL DEFAULT 'PENDING'");
            db.execSQL("ALTER TABLE service_bookings ADD COLUMN payment_method TEXT");
            db.execSQL("ALTER TABLE service_bookings ADD COLUMN total_amount REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE service_bookings ADD COLUMN updated_at_epoch_millis INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE service_bookings ADD COLUMN cancelled_at_epoch_millis INTEGER");

            db.execSQL("UPDATE room_bookings SET updated_at_epoch_millis = created_at_epoch_millis WHERE updated_at_epoch_millis = 0");
            db.execSQL("UPDATE service_bookings SET updated_at_epoch_millis = created_at_epoch_millis WHERE updated_at_epoch_millis = 0");
        }
    };
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE services ADD COLUMN imageRef TEXT");
        }
    };

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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
        db.execSQL("UPDATE room_types SET imageRef = 'ocean_view_suite' WHERE name = 'Ocean View Suite' AND (imageRef IS NULL OR imageRef = '')");
        db.execSQL("UPDATE room_types SET imageRef = 'deluxe_room' WHERE name = 'Deluxe Room' AND (imageRef IS NULL OR imageRef = '')");
        db.execSQL("UPDATE room_types SET imageRef = 'garden_suite' WHERE name = 'Garden Suite' AND (imageRef IS NULL OR imageRef = '')");

        db.execSQL(
                "INSERT INTO services(name, category, description, price, imageRef) " +
                        "SELECT 'Spa Massage','SPA','60-minute relaxation massage.',150.0,'service_spa_massage' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Spa Massage')"
        );
        db.execSQL(
                "INSERT INTO services(name, category, description, price, imageRef) " +
                        "SELECT 'Fine Dining','DINING','Chef-curated multi-course dinner.',200.0,'service_fine_dining' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Fine Dining')"
        );
        db.execSQL(
                "INSERT INTO services(name, category, description, price, imageRef) " +
                        "SELECT 'Poolside Cabanas','CABANAS','Reserved cabana access with refreshments.',100.0,'service_poolside_cabanas' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Poolside Cabanas')"
        );
        db.execSQL(
                "INSERT INTO services(name, category, description, price, imageRef) " +
                        "SELECT 'Guided Beach Tour','TOURS','Local guide for an unforgettable beach experience.',180.0,'service_guided_beach_tour' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Guided Beach Tour')"
        );
        db.execSQL("UPDATE services SET imageRef = 'service_spa_massage' WHERE name = 'Spa Massage' AND (imageRef IS NULL OR imageRef = '')");
        db.execSQL("UPDATE services SET imageRef = 'service_fine_dining' WHERE name = 'Fine Dining' AND (imageRef IS NULL OR imageRef = '')");
        db.execSQL("UPDATE services SET imageRef = 'service_poolside_cabanas' WHERE name = 'Poolside Cabanas' AND (imageRef IS NULL OR imageRef = '')");
        db.execSQL("UPDATE services SET imageRef = 'service_guided_beach_tour' WHERE name = 'Guided Beach Tour' AND (imageRef IS NULL OR imageRef = '')");
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

