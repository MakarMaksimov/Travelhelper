package com.example.travelhelper;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
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
import java.util.concurrent.TimeUnit;

public class FlightCheckWorker extends Worker {
    private static final String TAG = "FlightCheckWorker";
    private static final String CHANNEL_ID = "flight_alerts";
    private static final int NOTIFICATION_ID = 1;

    public FlightCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        createNotificationChannel(context);
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
        String departureTime = (String) trip.get("departure");

        if (flightNumber == null || departureTime == null) return;

        SharedPreferences prefs = context.getSharedPreferences("FlightTimes", Context.MODE_PRIVATE);
        String lastSavedTime = prefs.getString(flightNumber, null);

        if (lastSavedTime != null && !lastSavedTime.equals(departureTime)) {
            sendNotification(context,
                    "Изменение времени рейса " + flightNumber,
                    "Новое время вылета: " + formatTime(departureTime));
        }

        setFlightAlarm(context, flightNumber, departureTime);
        prefs.edit().putString(flightNumber, departureTime).apply();
    }

    private void setFlightAlarm(Context context, String flightNumber, String isoTime) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            Date departureDate = isoFormat.parse(isoTime);
            long alarmTime = departureDate.getTime() - TimeUnit.HOURS.toMillis(5);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Нет разрешения на точные будильники");
                    return;
                }
            }

            Intent intent = new Intent(context, FlightAlarmReceiver.class);
            intent.putExtra("flight_number", flightNumber);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    flightNumber.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка установки будильника", e);
        }
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

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Flight Alerts",NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getApplicationContext()
                    .getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel flightChannel = new NotificationChannel(
                    "flight_alerts",
                    "Flight Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationChannel alarmChannel = new NotificationChannel(
                    "alarm_channel",
                    "Flight Alarm",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alarmChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build());

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(flightChannel);
            manager.createNotificationChannel(alarmChannel);
        }
    }
}