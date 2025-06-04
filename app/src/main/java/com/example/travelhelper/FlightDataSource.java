package com.example.travelhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FlightDataSource {
    private SQLiteDatabase database;
    private FlightDatabaseHelper dbHelper;

    public FlightDataSource(Context context) {
        dbHelper = new FlightDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Добавление нового рейса
    public long addTrip(String flightNumber, String airport, String date, String status) {
        ContentValues values = new ContentValues();
        values.put(FlightDatabaseHelper.COLUMN_FLIGHT_NUMBER, flightNumber);
        values.put(FlightDatabaseHelper.COLUMN_AIRPORT, airport);
        values.put(FlightDatabaseHelper.COLUMN_DEPARTURE_DATE, date);
        values.put(FlightDatabaseHelper.COLUMN_STATUS, status);

        return database.insert(FlightDatabaseHelper.TABLE_FLIGHTS, null, values);
    }

    // Получение всех рейсов по статусу
    public Cursor getFlightsByStatus(String status) {
        String[] columns = {
                FlightDatabaseHelper.COLUMN_ID,
                FlightDatabaseHelper.COLUMN_FLIGHT_NUMBER,
                FlightDatabaseHelper.COLUMN_AIRPORT,
                FlightDatabaseHelper.COLUMN_DEPARTURE_DATE,
                FlightDatabaseHelper.COLUMN_STATUS
        };

        String selection = FlightDatabaseHelper.COLUMN_STATUS + " = ?";
        String[] selectionArgs = { status };

        return database.query(
                FlightDatabaseHelper.TABLE_FLIGHTS,
                columns,
                selection,
                selectionArgs,
                null, null, FlightDatabaseHelper.COLUMN_DEPARTURE_DATE + " ASC");
    }

    // Обновление статуса рейса
    public boolean updateFlightStatus(long id, String newStatus) {
        ContentValues values = new ContentValues();
        values.put(FlightDatabaseHelper.COLUMN_STATUS, newStatus);

        String whereClause = FlightDatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };

        return database.update(FlightDatabaseHelper.TABLE_FLIGHTS, values, whereClause, whereArgs) > 0;
    }

    // Полное удаление рейса из базы данных
    public boolean deleteFlight(long id) {
        String whereClause = FlightDatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };

        return database.delete(FlightDatabaseHelper.TABLE_FLIGHTS, whereClause, whereArgs) > 0;
    }

    // Поиск рейсов по номеру
    public Cursor searchFlightsByNumber(String flightNumber) {
        String[] columns = {
                FlightDatabaseHelper.COLUMN_ID,
                FlightDatabaseHelper.COLUMN_FLIGHT_NUMBER,
                FlightDatabaseHelper.COLUMN_AIRPORT,
                FlightDatabaseHelper.COLUMN_DEPARTURE_DATE,
                FlightDatabaseHelper.COLUMN_STATUS
        };

        String selection = FlightDatabaseHelper.COLUMN_FLIGHT_NUMBER + " LIKE ?";
        String[] selectionArgs = { "%" + flightNumber + "%" };

        return database.query(
                FlightDatabaseHelper.TABLE_FLIGHTS,
                columns,
                selection,
                selectionArgs,
                null, null, FlightDatabaseHelper.COLUMN_DEPARTURE_DATE + " ASC");
    }
}
