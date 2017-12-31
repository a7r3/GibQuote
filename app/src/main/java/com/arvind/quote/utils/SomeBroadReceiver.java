package com.arvind.quote.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arvind.quote.services.NotificationService;

import java.util.Calendar;

public class SomeBroadReceiver extends BroadcastReceiver {

    private String TAG = "SomeBroadReceiver";

    private AlarmManager alarmManager;
    private PendingIntent notifPendingIntent;
    private SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case "android.intent.action.BOOT_COMPLETED":
                case "com.arvind.quote.TIME_SET_BY_USER":
                    Intent someIntent = new Intent(context, SomeBroadReceiver.class);
                    someIntent.setAction("com.arvind.quote.SHOW_QUOTE");
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    Log.d(TAG, "Feels good to be back");
                    int hour = sharedPreferences.getInt("QOTD_HOUR", 0);
                    int minute = sharedPreferences.getInt("QOTD_MIN", 0);
                    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    notifPendingIntent = PendingIntent.getBroadcast(context, 0, someIntent, 0);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    Log.i(TAG, calendar.getTime().toString());
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY,
                            notifPendingIntent);
                    break;
                case "com.arvind.quote.SHOW_QUOTE":
                    Log.i(TAG, "Waking up Notification Service");
                    context.startService(new Intent(context, NotificationService.class));
                default:
                    break;
            }
        }
    }
}
