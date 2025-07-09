package com.example.android.ainotesapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver {
    public void onReceive(Context context, Intent intent) {
        String noteTitle = intent.getStringExtra("noteTitle");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "nobi_channel", "Nobi Reminders", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "nobi_channel")
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle("Nobi Reminder ⏰")
                .setContentText("Don't forget: " + noteTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        int notificationId = (int) (System.currentTimeMillis() % 10000);
        notificationManager.notify(notificationId, builder.build());
    }
}
