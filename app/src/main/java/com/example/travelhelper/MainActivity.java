package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
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

        email = findViewById(R.id.EmailAdress);
        password = findViewById(R.id.Password);
        signin = findViewById(R.id.SigningInButton);
        signup = findViewById(R.id.SigningUpButton);

        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sp.getString("userId", null);

        if (userId != null) {
            startTravelHelperHome(userId);
            setupBackgroundTasks();
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
        setupBackgroundTasks();
    }

    private void setupBackgroundTasks() {
        createNotificationChannel();
        setupWorkManager();
        if (checkExactAlarmPermission()) {
            setupAlarmManager();
        }
        runImmediateCheck();
    }

    private boolean checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmPermission();
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestExactAlarmPermission() {
        try {
            Intent intent = new Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("MainActivity", "Failed to request exact alarm permission", e);
            showManualPermissionInstructions();
        }
    }

    private void showManualPermissionInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Требуется разрешение");
        builder.setMessage("Для точных уведомлений необходимо предоставить разрешение на точные будильники. " +
                "Пожалуйста, перейдите в Настройки > Приложения > [Ваше приложение] > Разрешения и включите 'Точные будильники'");
        builder.setPositiveButton("Открыть настройки", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        builder.setNegativeButton("Позже", null);
        builder.show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setupAlarmManager() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, TripAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e("MainActivity", "Failed to set exact alarm", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Flight Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for upcoming and completed flights");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void setupWorkManager() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                TripCheckWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_flight_check",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }

    private void runImmediateCheck() {
        OneTimeWorkRequest immediateWork = new OneTimeWorkRequest.Builder(TripCheckWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this)
                .beginUniqueWork("initial_flight_check",
                        ExistingWorkPolicy.REPLACE,
                        immediateWork)
                .enqueue();
    }
}
