package com.example.travelhelper;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Signingin extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private HashMap<String, Object> UpcomingTrips;
    private HashMap<String, Object> PlannedTrips;
    private EditText passwordsectime;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signingin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.PlannedButton), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button signin = findViewById(R.id.SigningInButton2);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switcher = new Intent(Signingin.this, MainActivity.class);
                Signingin.this.startActivity(switcher);
                finish();
            }
        });

        email = findViewById(R.id.EmailAdress2);
        password = findViewById(R.id.Password2);
        passwordsectime = findViewById(R.id.editTextTextPassword);
        signup = findViewById(R.id.SigningUpButton2);
        UpcomingTrips = new HashMap<>();
        PlannedTrips = new HashMap<>();
       signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateregistration();
            }
        });


    }


    private boolean validateregistration(){
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();
        String password2Input = passwordsectime.getText().toString().trim();
        if(emailInput.isEmpty() || (emailInput.contains("@") && emailInput.contains("mail.ru") && emailInput.contains("ya.ru") && emailInput.contains("yandex.ru") && emailInput.contains("gmail.ru") && emailInput.contains("rambler.ru") && emailInput.contains("outlook.ru"))){
            email.setError("Check your E-mail!");
            return false;
        }
        else if (passwordInput.isEmpty() || passwordInput.length() <= 6){
            password.setError("Password is too short!");
            return false;
        }
        else if(!(password2Input.equals(passwordInput))){
            passwordsectime.setError("Passwords don't match!");
            return false;
        }
        else{
            SharedPreferences sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("userId", email.getText().toString());
            editor.apply();
            String userId = registerUser(email.getText().toString(), password.getText().toString());
            Intent switcher = new Intent(Signingin.this, TravelHelperHome.class);
            switcher.putExtra("userId", userId);
            Signingin.this.startActivity(switcher);
            finish();
            return true;
        }
    }

    private String registerUser(String email, String password) {
        // Создаем объект пользователя
        users user = new users(email, password);

        // Добавляем в Firestore с email как ID документа
        db.collection("users")
                .document(email)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User registered: " + email);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error registering user", e);
                });
        return email;
    }

}