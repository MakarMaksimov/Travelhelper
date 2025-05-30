package com.example.travelhelper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.google.firebase.firestore.QueryDocumentSnapshot;

public class GetInfo {
    public class FlightsInfo {
        private String id;
        public List<HashMap<String, Object>> trips = new ArrayList<>();
        public FlightsInfo(){}

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
        flights.setTrips(flights.id);

        Date date = new Date(300, 0, 0);

        HashMap<String, Object> nearesttrip = new HashMap<>();
        nearesttrip.put("date", date);

        for (HashMap<String, Object> trip : flights.trips) {
            Date tripdate = (Date)trip.get("date");
            Date nearestdate = (Date)nearesttrip.get("date");
            if (tripdate.getTime() < nearestdate.getTime()) {
                nearesttrip.putAll(trip);
            }
        }

        // nearesttrip - ближайшая поездка

    }
}
