package com.example.ecostay.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ecostay.data.dao.BookingDao;
import com.example.ecostay.data.dao.DeviceDao;
import com.example.ecostay.data.dao.FaqDao;
import com.example.ecostay.data.dao.MaintenanceTipDao;
import com.example.ecostay.data.dao.RepairStatusDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.dao.SupportMessageDao;
import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.BookingEntity;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.data.entity.FaqEntity;
import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.data.entity.RepairStatusEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.data.entity.SupportMessageEntity;
import com.example.ecostay.data.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                UserEntity.class,
                DeviceEntity.class,
                ServiceEntity.class,
                BookingEntity.class,
                RepairStatusEntity.class,
                FaqEntity.class,
                MaintenanceTipEntity.class,
                SupportMessageEntity.class
        },
        version = 8,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "techcare.db";
    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService DATABASE_WRITE_EXECUTOR =
            Executors.newSingleThreadExecutor();

    public abstract UserDao userDao();

    public abstract DeviceDao deviceDao();

    public abstract ServiceDao serviceDao();

    public abstract BookingDao bookingDao();

    public abstract RepairStatusDao repairStatusDao();

    public abstract FaqDao faqDao();

    public abstract MaintenanceTipDao maintenanceTipDao();

    public abstract SupportMessageDao supportMessageDao();

    public static ExecutorService getWriteExecutor() {
        return DATABASE_WRITE_EXECUTOR;
    }

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME
                            )
                            .addMigrations(
                                    DatabaseMigrations.MIGRATION_4_5,
                                    DatabaseMigrations.MIGRATION_5_6,
                                    DatabaseMigrations.MIGRATION_6_7,
                                    DatabaseMigrations.MIGRATION_7_8
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    seedCatalogData(db);
                                    AdminUserSeeder.seedAdminUser(db);
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    AdminUserSeeder.seedAdminUser(db);
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedCatalogData(@NonNull SupportSQLiteDatabase db) {
        seedServices(db);
        seedFaqs(db);
        seedMaintenanceTips(db);
    }

    private static void seedServices(@NonNull SupportSQLiteDatabase db) {
        insertService(db, "Smartphone", "Mobile Screen Replacement",
                "Replace cracked or unresponsive smartphone screens.", 8500.0);
        insertService(db, "Smartphone", "Mobile Battery Replacement",
                "Restore battery life with a genuine replacement.", 4500.0);
        insertService(db, "Smartphone", "Charging Port Repair",
                "Fix loose or damaged charging ports.", 3500.0);

        insertService(db, "Laptop", "Laptop Not Charging",
                "Diagnose and repair charging circuit issues.", 5500.0);
        insertService(db, "Laptop", "Laptop Keyboard Replacement",
                "Replace faulty or damaged keyboards.", 6500.0);
        insertService(db, "Laptop", "Laptop Display Issue",
                "Fix flickering, dead pixels, or backlight problems.", 12000.0);

        insertService(db, "Television", "TV Display Issue",
                "Repair panel, backlight, or display faults.", 15000.0);
        insertService(db, "Television", "TV Sound Issue",
                "Fix speakers, audio board, or sound output problems.", 7500.0);

        insertService(db, "Air Conditioner", "AC Gas Refill",
                "Refrigerant top-up and leak inspection.", 9000.0);
        insertService(db, "Air Conditioner", "AC Cleaning Service",
                "Deep clean filters, coils, and drainage.", 4000.0);

        insertService(db, "Refrigerator", "Refrigerator Cooling Issue",
                "Diagnose thermostat, compressor, or coolant faults.", 11000.0);
        insertService(db, "Refrigerator", "Compressor Checkup",
                "Inspect and service refrigerator compressor.", 8500.0);

        insertService(db, "Washing Machine", "Washing Machine Drum Issue",
                "Repair drum bearings, alignment, or motor faults.", 9500.0);
        insertService(db, "Washing Machine", "Water Drainage Issue",
                "Fix pump, hose, or drainage blockages.", 5000.0);
    }

    private static void insertService(@NonNull SupportSQLiteDatabase db, String deviceType,
                                      String serviceName, String description, double price) {
        db.execSQL(
                "INSERT INTO services(deviceType, serviceName, description, estimatedPrice) " +
                        "SELECT ?, ?, ?, ? WHERE NOT EXISTS (" +
                        "SELECT 1 FROM services WHERE serviceName = ? AND deviceType = ?)",
                new Object[]{deviceType, serviceName, description, price, serviceName, deviceType}
        );
    }

    private static void seedFaqs(@NonNull SupportSQLiteDatabase db) {
        insertFaq(db, "How do I submit a repair request?",
                "Browse services by device type, tap Request Service, select your device, describe the issue, and submit.");
        insertFaq(db, "What is the difference between pickup and drop-off?",
                "Pickup means our team collects your device from your address. Drop-off means you bring the device to our service center.");
        insertFaq(db, "How can I track my repair status?",
                "Open My Bookings, select a booking, and view the repair progress timeline and status history.");
        insertFaq(db, "Is there a warranty on repairs?",
                "Yes. Most repairs include a 30-day service warranty. Ask support for device-specific terms.");
        insertFaq(db, "How are technicians assigned?",
                "A specialist is assigned automatically based on your device type when you submit a request.");
        insertFaq(db, "What payment methods are accepted?",
                "Pay at the service center or upon pickup after the repair is completed.");
    }

    private static void insertFaq(@NonNull SupportSQLiteDatabase db, String question, String answer) {
        db.execSQL(
                "INSERT INTO faqs(question, answer) SELECT ?, ? WHERE NOT EXISTS (" +
                        "SELECT 1 FROM faqs WHERE question = ?)",
                new Object[]{question, answer, question}
        );
    }

    private static void seedMaintenanceTips(@NonNull SupportSQLiteDatabase db) {
        insertTip(db, "Smartphone", "Use a quality case",
                "A protective case reduces screen and port damage from drops.");
        insertTip(db, "Smartphone", "Avoid overnight charging",
                "Unplug when fully charged to extend battery health.");

        insertTip(db, "Laptop", "Keep vents clear",
                "Dust buildup causes overheating. Clean vents every few months.");
        insertTip(db, "Laptop", "Use surge protection",
                "Protect your laptop charger and board from power spikes.");

        insertTip(db, "Television", "Ventilate your TV",
                "Leave space behind the TV for airflow to prevent overheating.");
        insertTip(db, "Television", "Adjust brightness",
                "Lower brightness settings can extend panel lifespan.");

        insertTip(db, "Air Conditioner", "Clean filters monthly",
                "Wash or replace AC filters to maintain cooling efficiency.");
        insertTip(db, "Air Conditioner", "Schedule annual service",
                "Professional cleaning prevents gas leaks and poor cooling.");

        insertTip(db, "Refrigerator", "Check door seals",
                "Worn seals let cold air escape and strain the compressor.");
        insertTip(db, "Refrigerator", "Do not overload",
                "Proper airflow inside the fridge keeps food evenly cooled.");

        insertTip(db, "Washing Machine", "Leave door open after use",
                "Air circulation prevents mold and bad odors in the drum.");
        insertTip(db, "Washing Machine", "Clean the lint filter",
                "Remove lint regularly to avoid drainage and motor issues.");
    }

    private static void insertTip(@NonNull SupportSQLiteDatabase db, String deviceType,
                                  String title, String description) {
        db.execSQL(
                "INSERT INTO maintenance_tips(deviceType, title, description) SELECT ?, ?, ? " +
                        "WHERE NOT EXISTS (SELECT 1 FROM maintenance_tips WHERE title = ? AND deviceType = ?)",
                new Object[]{deviceType, title, description, title, deviceType}
        );
    }
}
