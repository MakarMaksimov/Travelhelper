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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button signup;
    private Button signin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.PlannedButton), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        DocumentReference newUserRef = db.collection("users").document();
        email = findViewById(R.id.EmailAdress);
        SharedPreferences sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sp.getString("userId", null);
       if(userId != null){
           Intent switcher = new Intent(MainActivity.this, TravelHelperHome.class);
           switcher.putExtra("userId", userId);
           MainActivity.this.startActivity(switcher);
           finish();
       }
       else {
            password=findViewById(R.id.Password);
            signin = findViewById(R.id.SigningInButton);
            signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("users")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(Objects.equals(document.getString("email"), email.getText().toString())){
                                                if(Objects.equals(document.getString("password"), password.getText().toString())) {
                                                    SharedPreferences sp1 = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sp1.edit();
                                                    editor.putString("userId", email.getText().toString());
                                                    editor.apply();
                                                    Intent switcher = new Intent(MainActivity.this, TravelHelperHome.class);
                                                    switcher.putExtra("userId", email.getText().toString());
                                                    MainActivity.this.startActivity(switcher);
                                                    finish();
                                                }
                                            }
                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });
                }
            });
            signup = findViewById(R.id.SigningUpButton);
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent switcher = new Intent(MainActivity.this, Signingin.class);
                    MainActivity.this.startActivity(switcher);
                    finish();
                }
            });
       }
    }

}
