package com.example.ecostay.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class PromotionWorker extends Worker {

    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";

    public PromotionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data input = getInputData();
        int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        String title = input.getString(KEY_TITLE);
        String message = input.getString(KEY_MESSAGE);

        NotificationHelper.showNotification(
                getApplicationContext(),
                NotificationHelper.CHANNEL_PROMOTIONS,
                notificationId,
                title == null ? "LuxeVista Promotions" : title,
                message == null ? "Check today's offers and exclusive offers in the app." : message,
                NotificationHelper.offersDeepLinkPendingIntent(getApplicationContext(), notificationId)
        );

        return Result.success();
    }
}

