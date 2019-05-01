package com.arvind.quote.utils.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.arvind.quote.BuildConfig;
import com.arvind.quote.R;
import com.arvind.quote.adapter.Quote;
import com.arvind.quote.services.CommonUtilService;
import com.arvind.quote.utils.SomeBroadReceiver;
import com.google.gson.Gson;

public class NotificationUtils {

    private static NotificationUtils notificationUtils;
    private final String channelId = "com.arvind.quote.QuoteNotifChannel";
    private Context context;
    private int notificationId;
    private NotificationManager notificationManager;

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

    public static synchronized NotificationUtils getInstance(Context context) {
        if (notificationUtils == null) {
            notificationUtils = new NotificationUtils(context);
        }
        return notificationUtils;
    }

    public void issueNotification(Quote quote) {
        Intent intent = new Intent(context, NotificationDialog.class);
        intent.putExtra("quoteText", quote.getQuoteText());
        intent.putExtra("authorText", quote.getAuthorText());
        // Specify the MIME type of the object to be thrown
        intent.setType("text/plain");

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                notificationId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent commonUtilIntent = new Intent(context, SomeBroadReceiver.class);
        // Converting Quote Object to Serialized (JSON), GSON FTW!
        commonUtilIntent.setAction(BuildConfig.APPLICATION_ID + ".COMMON_UTILS");
        commonUtilIntent.putExtra(CommonUtilService.QUOTE_KEY, new Gson().toJson(quote));

        PendingIntent pendingShareIntent = PendingIntent
                .getBroadcast(context,
                        0,
                        commonUtilIntent
                                .putExtra(CommonUtilService.INTENT_EXEC_KEY, CommonUtilService.SHARE_QUOTE),
                        PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent pendingFavIntent = PendingIntent
                .getBroadcast(context,
                        1,
                        commonUtilIntent.putExtra(CommonUtilService.INTENT_EXEC_KEY, CommonUtilService.ADD_FAV_QUOTE),
                        PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelId)
                .setContentText(quote.getQuoteText()) // Notification Content
                .setContentTitle("Quote by " + quote.getAuthorText()) // Notification Title
                // Seems like NotificationCompat mananges to
                // Not involve Channel Stuffs in pre-26
                .setChannelId(channelId) // I want to go to this channel
                .setSmallIcon(R.mipmap.ic_launcher_round) // Icon which'd appear to left of AppTitle
                .setStyle(new NotificationCompat
                        .BigTextStyle()
                        .setBigContentTitle("Quote by " + quote.getAuthorText())
                        .setSummaryText("Quote of the Day"))
                .setTicker("Random Quote of the day, for you")
                .setAutoCancel(true)
                .addAction(R.drawable.ic_share_black_24dp, "Share", pendingShareIntent)
                .addAction(R.drawable.star_off, "Star this", pendingFavIntent)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            notification.setColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
        notificationManager.notify(notificationId++, notification.build());
    }
}
