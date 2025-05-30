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

    public TripCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        createNotificationChannel(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Запуск фоновой проверки поездок");
        checkAndMoveTrips();
        return Result.success();
    }

    private void checkAndMoveTrips() {
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
            Date tripDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
            long timeDiff = tripDate.getTime() - System.currentTimeMillis();

            if (timeDiff <= DAYS_7_MS && timeDiff > 0) {
                moveTrip(doc);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обработки поездки", e);
        }
    }

    private void moveTrip(QueryDocumentSnapshot doc) {
        String tripName = doc.getString("trip_number");
        String userPath = doc.getReference().getParent().getParent().getPath();

        db.runTransaction(transaction -> {
            transaction.set(db.document(userPath + "/upcoming_trips/" + doc.getId()), doc.getData());
            transaction.delete(doc.getReference());
            return null;
        }).addOnSuccessListener(__ -> {
            sendNotification(tripName);
            Log.d(TAG, "Поездка перемещена: " + tripName);
        }).addOnFailureListener(e -> Log.e(TAG, "Ошибка перемещения", e));
    }

    private void sendNotification(String tripName) {
        NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Скоро поездка!")
                .setContentText("До поездки " + tripName + " осталось меньше недели")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Уведомления о поездках",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Уведомления о предстоящих поездках");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}