package com.example.travelhelper;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "trip_alerts";
    private EditText email, password;
    private Button signup, signin;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация UI
        email = findViewById(R.id.EmailAdress);
        password = findViewById(R.id.Password);
        signin = findViewById(R.id.SigningInButton);
        signup = findViewById(R.id.SigningUpButton);

        // Проверка авторизации
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId != null) {
            startTravelHelperHome(userId);
            setupBackgroundTasks(); // Запускаем фоновые задачи после входа
        } else {
            setupAuthButtons();
        }
    }

    private void startTravelHelperHome(String userId) {
        Intent intent = new Intent(this, TravelHelperHome.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    private void setupAuthButtons() {
        signin.setOnClickListener(v -> checkUserCredentials());
        signup.setOnClickListener(v -> {
            startActivity(new Intent(this, Signingin.class));
            finish();
        });
    }

    private void checkUserCredentials() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        db.collection("users")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        if (Objects.equals(doc.getString("password"), userPassword)) {
                            saveUserIdAndStart(userEmail);
                        }
                    } else {
                        Log.w(TAG, "Auth error", task.getException());
                    }
                });
    }

    private void saveUserIdAndStart(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putString("userId", userId);
        editor.apply();

        startTravelHelperHome(userId);
        setupBackgroundTasks(); // Запускаем задачи после успешной авторизации
    }

    // ==================== Фоновые задачи ====================
    private void setupBackgroundTasks() {
        createNotificationChannel();
        setupWorkManager();
        setupAlarmManager();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Trip Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void setupWorkManager() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                TripCheckWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_trip_check",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    private void setupAlarmManager() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, TripAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8); // 8:00 утра
        calendar.set(Calendar.MINUTE, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
