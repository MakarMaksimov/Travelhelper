package com.example.travelhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class TripAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TripAlarm", "Запуск проверки рейсов по расписанию");

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TripCheckWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context)
                .beginUniqueWork("immediate_trip_check",
                        ExistingWorkPolicy.REPLACE,
                        workRequest)
                .enqueue();
    }
}
