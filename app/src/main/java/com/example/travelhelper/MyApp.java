package com.example.travelhelper;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlightCheckScheduler.scheduleFlightChecks(this);
    }
}