package edu.temple.widgetapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        alarmIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                AppWidgetManager.getInstance(context).getAppWidgetIds(
                        new ComponentName(context.getPackageName(), ExampleWidget.class.getName())
                ));

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        Log.d("Bootup broadcast", "Received");
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("alarm_set", false))
            am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 5000, 60000, pi);
    }
}
