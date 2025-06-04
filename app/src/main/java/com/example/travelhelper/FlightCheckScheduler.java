package com.example.travelhelper;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class FlightCheckScheduler {
    private static final String WORK_TAG = "flight_check_worker";
    public static void scheduleFlightChecks(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        FlightCheckWorker.class,
                        5, TimeUnit.HOURS,
                        15, TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }
}