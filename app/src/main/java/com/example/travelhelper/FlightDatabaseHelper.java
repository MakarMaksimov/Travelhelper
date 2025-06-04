package com.example.travelhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FlightDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "flights.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_FLIGHTS = "flights";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FLIGHT_NUMBER = "flight_number";
    public static final String COLUMN_AIRPORT = "airport";
    public static final String COLUMN_DEPARTURE_DATE = "departure_date";
    public static final String COLUMN_STATUS = "status";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FLIGHTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FLIGHT_NUMBER + " TEXT NOT NULL, " +
                    COLUMN_AIRPORT + " TEXT NOT NULL, " +
                    COLUMN_DEPARTURE_DATE + " TEXT NOT NULL, " +
                    COLUMN_STATUS + " TEXT DEFAULT 'active'" +
                    ");";

    public FlightDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLIGHTS);
        onCreate(db);
    }
}