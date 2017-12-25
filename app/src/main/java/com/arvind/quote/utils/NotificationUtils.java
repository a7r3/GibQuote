package com.arvind.quote.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.arvind.quote.NotificationDialog;
import com.arvind.quote.R;
import com.arvind.quote.adapter.Quote;

public class NotificationUtils {

    private Context context;
    private int notificationId;
    private String channelId = "com.arvind.quote.QuoteNotifChannel";
    private NotificationManager notificationManager;

    // Static instance of Notification Utility
    private static NotificationUtils notificationUtilsInstance;

    private NotificationUtils(Context context) {
        this.context = context;
        notificationId = 1;
        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 8.0 Implements Notification Channel for providing precise control to the user
        // User is able to block certain notifications by their Category
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId,
                    "GibQuote:QoTD",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(R.color.green); // <3
            notificationChannel.enableVibration(true);
            // Allow notifications to be visible even if Security Features are enabled
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // NotificationManager would pipe any incoming notifications through this channel
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    // Make sure that we get a single Notification Utility instance throughout
    // the Application (Singleton Instance they said)
    public static synchronized NotificationUtils getInstance(Context context) {
        if(notificationUtilsInstance == null)
            notificationUtilsInstance = new NotificationUtils(context);
        return notificationUtilsInstance;
    }

    public void issueNotification(Quote quote) {
        String quoteText = quote.getQuoteText();
        String authorText = quote.getAuthorText();
        Intent intent = new Intent(context, NotificationDialog.class);
        intent.putExtra("quoteText", quoteText);
        intent.putExtra("authorText", authorText);
        // Specify the MIME type of the object to be thrown
        intent.setType("text/plain");

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                notificationId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notif = new NotificationCompat.Builder(context, channelId)
                .setContentText(quoteText) // Notification Content
                .setContentTitle("Quote by " + authorText) // Notification Title
                // Seems like NotificationCompat mananges to
                // Not involve Channel Stuffs in pre-26
                .setChannelId(channelId) // I want to go to this channel
                .setSmallIcon(R.mipmap.ic_launcher_round) // Icon which'd appear to left of AppTitle
                .setStyle(new NotificationCompat
                        .BigTextStyle()
                        .setBigContentTitle("Quote by " + authorText)
                        .setSummaryText("Quote of the Day"))
                .setTicker("Random Quote of the day, for you")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            notif.setColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
        notificationManager.notify(notificationId++, notif.build());
    }
}
