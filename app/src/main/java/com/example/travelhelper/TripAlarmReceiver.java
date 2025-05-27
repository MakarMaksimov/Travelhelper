package com.example.travelhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class TripAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TripAlarm", "Точное время срабатывания");
        WorkManager.getInstance(context)
                .beginWith(OneTimeWorkRequest.from(TripCheckWorker.class))
                .enqueue();
    }
}
