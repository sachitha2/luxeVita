package com.example.ecostay.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BookingReminderWorker extends Worker {

    public static final String KEY_NOTIFICATION_ID = "notification_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";

    public BookingReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data input = getInputData();
        int notificationId = input.getInt(KEY_NOTIFICATION_ID, (int) System.currentTimeMillis());
        String title = input.getString(KEY_TITLE);
        String message = input.getString(KEY_MESSAGE);

        NotificationHelper.showNotification(
                getApplicationContext(),
                NotificationHelper.CHANNEL_BOOKINGS,
                notificationId,
                title == null ? "Upcoming booking" : title,
                message == null ? "Your booking is coming up soon." : message,
                NotificationHelper.offersDeepLinkPendingIntent(getApplicationContext(), notificationId)
        );

        return Result.success();
    }
}

