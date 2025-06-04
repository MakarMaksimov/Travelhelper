package com.example.travelhelper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class FlightCheckWorker extends Worker {
    private static final String TAG = "FlightCheckWorker";
    private static final String CHANNEL_ID = "flight_alerts";
    private static final int NOTIFICATION_ID = 1;
    private static final String PREFS_NAME = "FlightTimes";

    public FlightCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        createNotificationChannel();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            GetInfo getInfo = new GetInfo();

            getInfo.getNearestTripInfo(context, new GetInfo.TripInfoCallback() {
                @Override
                public void onTripInfoReady(Map<String, Object> tripInfo) {
                    checkForScheduleChanges(context, tripInfo);
                }
                @Override
                public void onError(Exception e) {
                    Log.e("FlightCheck", "Error getting trip info", e);
                }
            });

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private void checkForScheduleChanges(Context context, Map<String, Object> trip) {
        String flightNumber = (String) trip.get("trip_number");
        String newDepartureTime = (String) trip.get("departure");

        if (flightNumber == null || newDepartureTime == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastSavedTime = prefs.getString(flightNumber, null);

        if (lastSavedTime != null && !lastSavedTime.equals(newDepartureTime)) {
            sendNotification(context,"Изменение времени рейса " + flightNumber,"Новое время вылета: " + formatTime(newDepartureTime));
        }
        prefs.edit().putString(flightNumber, newDepartureTime).apply();
    }

    private String formatTime(String isoTime) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm, dd MMMM", Locale.getDefault());
            Date date = isoFormat.parse(isoTime);
            return displayFormat.format(date);
        } catch (Exception e) {
            return isoTime;
        }
    }

    private void sendNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e("FlightCheckWorker", "Ошибка отправки уведомления", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Flight Alerts",NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getApplicationContext()
                    .getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}