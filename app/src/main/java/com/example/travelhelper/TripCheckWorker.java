package com.example.travelhelper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TripCheckWorker extends Worker {
    private static final String TAG = "TripCheckWorker";
    private static final long DAYS_7_MS = TimeUnit.DAYS.toMillis(7);
    private static final String CHANNEL_ID = "trip_alerts";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FlightDataSource flightDataSource;

    public TripCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        createNotificationChannel(context);
        flightDataSource = new FlightDataSource(context);
        flightDataSource.open();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Запуск фоновой проверки поездок");
        checkAndProcessTrips();
        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        flightDataSource.close();
    }

    private void checkAndProcessTrips() {
        db.collectionGroup("planned_trips")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            processTrip(doc);
                        }
                    } else {
                        Log.e(TAG, "Ошибка Firestore", task.getException());
                    }
                });
    }

    private void processTrip(QueryDocumentSnapshot doc) {
        try {
            String dateStr = doc.getString("date");
            if (dateStr == null) {
                Log.w(TAG, "Поездка без даты: " + doc.getId());
                return;
            }

            String flightNumber = doc.getString("trip_number");
            String airport = doc.getString("destination");
            if (flightNumber == null || airport == null) {
                Log.w(TAG, "Поездка без номера или аэропорта: " + doc.getId());
                return;
            }

            Date tripDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
            long timeDiff = tripDate.getTime() - System.currentTimeMillis();

            if (timeDiff <= DAYS_7_MS && timeDiff > 0) {
                moveToUpcoming(doc, flightNumber);
            } else if (timeDiff <= 0) {
                moveToCompleted(doc, flightNumber, airport, dateStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обработки поездки " + doc.getId(), e);
        }
    }

    private void moveToUpcoming(QueryDocumentSnapshot doc, String flightNumber) {
        String userPath = doc.getReference().getParent().getParent().getPath();
        String newPath = userPath + "/upcoming_trips/" + doc.getId();

        db.document(newPath).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                db.runTransaction(transaction -> {
                    transaction.set(db.document(newPath), doc.getData());
                    transaction.delete(doc.getReference());
                    return null;
                }).addOnSuccessListener(__ -> {
                    sendNotification(flightNumber,
                            "До вылета рейса " + flightNumber + " осталось меньше недели!");
                    Log.d(TAG, "Рейс перемещен в upcoming: " + flightNumber);
                }).addOnFailureListener(e ->
                        Log.e(TAG, "Ошибка перемещения рейса в upcoming", e));
            }
        });
    }

    private void moveToCompleted(QueryDocumentSnapshot doc, String flightNumber,
                                 String airport, String dateStr) {
        // Сохраняем в SQL базу
        long insertId = flightDataSource.addTrip(
                flightNumber,
                airport,
                dateStr,
                "Завершен"
        );

        if (insertId != -1) {
            doc.getReference().delete()
                    .addOnSuccessListener(__ -> {
                        sendNotification(flightNumber,
                                "Рейс " + flightNumber + " завершен и сохранен в историю");
                        Log.d(TAG, "Рейс сохранен в SQL и удален из Firestore: " + flightNumber);
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Ошибка удаления завершенного рейса", e));
        } else {
            Log.e(TAG, "Ошибка сохранения рейса в SQL базу: " + flightNumber);
        }
    }

    private void sendNotification(String flightNumber, String message) {
        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Уведомление о рейсе " + flightNumber)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Уведомления о рейсах",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Уведомления о предстоящих и завершенных рейсах");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}