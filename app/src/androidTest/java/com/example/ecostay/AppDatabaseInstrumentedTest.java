package com.example.ecostay;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.entity.ServiceEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseInstrumentedTest {

    private AppDatabase database;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void serviceDao_insertsAndReads() {
        ServiceEntity service = new ServiceEntity();
        service.deviceType = "Smartphone";
        service.serviceName = "Test Service";
        service.description = "Test description";
        service.estimatedPrice = 1000.0;

        database.serviceDao().insert(service);
        List<ServiceEntity> all = database.serviceDao().getAll();

        assertFalse(all.isEmpty());
        assertTrue(all.get(0).serviceName.contains("Test"));
    }
}
