package com.example.ecostay.notifications;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Data;

import java.util.concurrent.TimeUnit;

public final class NotificationScheduler {

    private static final String UNIQUE_PROMOTIONS_WORK = "ecoStay_promotions";

    private NotificationScheduler() {
    }

    public static void schedulePeriodicPromotions(Context context) {
        WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());

        Data input = new Data.Builder()
                .putString(PromotionWorker.KEY_TITLE, "Exclusive LuxeVista Offer")
                .putString(PromotionWorker.KEY_MESSAGE, "New promotions may be available today. Tap to explore.")
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                PromotionWorker.class,
                7, TimeUnit.DAYS
        )
                .setInputData(input)
                .build();

        workManager.enqueueUniquePeriodicWork(
                UNIQUE_PROMOTIONS_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    public static void scheduleBookingReminder(
            Context context,
            String uniqueWorkName,
            long delayMillis,
            int notificationId,
            String title,
            String message
    ) {
        if (delayMillis < 0) return;

        WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());

        Data input = new Data.Builder()
                .putInt(BookingReminderWorker.KEY_NOTIFICATION_ID, notificationId)
                .putString(BookingReminderWorker.KEY_TITLE, title)
                .putString(BookingReminderWorker.KEY_MESSAGE, message)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(BookingReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(input)
                .build();

        workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }

    public static void cancelBookingReminder(Context context, String uniqueWorkName) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(uniqueWorkName);
    }
}

