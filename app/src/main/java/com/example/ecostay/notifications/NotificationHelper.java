package com.example.ecostay.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.ecostay.R;
import com.example.ecostay.ui.OffersActivity;

public final class NotificationHelper {

    public static final String CHANNEL_BOOKINGS = "ecoStay_bookings";
    public static final String CHANNEL_PROMOTIONS = "ecoStay_promotions";

    private NotificationHelper() {
    }

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel bookingChannel = new NotificationChannel(
                CHANNEL_BOOKINGS,
                "Booking reminders",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(bookingChannel);

        NotificationChannel promoChannel = new NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promotions",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(promoChannel);
    }

    public static void showNotification(
            Context context,
            String channelId,
            int notificationId,
            String title,
            String message,
            @Nullable PendingIntent contentIntent
    ) {
        ensureChannels(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (contentIntent != null) builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }

    public static PendingIntent offersDeepLinkPendingIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, OffersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}

