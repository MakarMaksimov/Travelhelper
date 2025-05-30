package com.example.travelhelper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.firebase.firestore.QueryDocumentSnapshot;

public class GetInfo {
    public class FlightsInfo {
        private String id;
        public List<HashMap<String, Object>> trips = new ArrayList<>();
        public FlightsInfo(){}
        public FlightsInfo(String email) {
            this.id = email;
        }

        public void setId(Context context) {
            SharedPreferences sp = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String userId = sp.getString("userId", null);
            if (userId == null) {
                throw new IllegalStateException("User ID not found in SharedPreferences");
            }
            this.id = userId;
        }
        public void setTrips(String email) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(email)
                    .collection("trips")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap<String, Object> tripData = (HashMap<String, Object>) document.getData();
                                this.trips.add(tripData);
                            }
                        } else {
                            Log.w("Firestore", "Error getting trips", task.getException());
                        }
                    });
        }
        public String getId() { return id; }
        public List<HashMap<String, Object>> getTrips() { return trips; }
    }
    public void Metod(Context context) {
        FlightsInfo flights = new FlightsInfo();
        flights.setId(context);
    }
}
