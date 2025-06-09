package com.example.travelhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class FlightAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String flightNumber = intent.getStringExtra("flight_number");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.free_icon_airplane_flight_6735552)
                .setContentTitle("Напоминание о рейсе")
                .setContentText("Рейс " + flightNumber + " через 5 часов!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        SharedPreferences sharedPref = context.getSharedPreferences("App_Prefs", Context.MODE_PRIVATE);
        String alarm_clock = sharedPref.getString("alarm_clock", "default_value");
        if (!alarm_clock.isEmpty() && alarm_clock.charAt(0) == 't') {
            NotificationManagerCompat.from(context).notify(flightNumber.hashCode(), builder.build());
        }
    }
}