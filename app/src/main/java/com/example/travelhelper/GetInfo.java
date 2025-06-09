package com.example.travelhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetInfo {
    private static final String TAG = "GetInfo";
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    static class UsersFlights {
        private String id;
        public List<Map<String, Object>> trips = new ArrayList<>();
        public UsersFlights(Context context) {
            SharedPreferences sp = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            this.id = sp.getString("userId", null);
            if (this.id == null) {
                throw new IllegalStateException("User ID not found in SharedPreferences");
            }
        }
        public void setTrips(String email, final TripLoadCallback callback) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(email)
                    .collection("trips")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                this.trips.add(document.getData());
                            }
                            callback.onTripsLoaded();
                        } else {
                            Log.w(TAG, "Error getting trips", task.getException());
                            callback.onError(task.getException());
                        }
                    });
        }
        public String getId() { return id; }
    }

    interface TripLoadCallback {
        void onTripsLoaded();
        void onError(Exception e);
    }

    static class AirportKey {
        private final Map<String, String> airportCodes = new HashMap<>();
        public AirportKey() {
            initializeAirportCodes();
        }
        private void initializeAirportCodes() {
            airportCodes.put("Sheremetyevo", "SVO");
            airportCodes.put("Vnukovo", "VKO");
            airportCodes.put("Domodedovo", "DME");
            airportCodes.put("Pulkovo", "LED");
            airportCodes.put("Balandino", "CEK");
            airportCodes.put("Sochi International Airport", "AER");
            airportCodes.put("Sochi Airport", "AER");
            airportCodes.put("Koltsovo", "SVX");
            airportCodes.put("Tolmachevo", "OVB");
            airportCodes.put("Platov", "ROV");
            airportCodes.put("Kazan International Airport", "KZN");
            airportCodes.put("Kazan Airport", "KZN");
            airportCodes.put("Krasnodar International Airport", "KRR");
            airportCodes.put("Krasnodar Airport", "KRR");
        }
    }
    public void getNearestTripInfo(Context context, final TripInfoCallback callback) {
        UsersFlights flights = new UsersFlights(context);

        flights.setTrips(flights.getId(), new TripLoadCallback() {
            @Override
            public void onTripsLoaded() {
                try {
                    Map<String, Object> nearestTrip = findNearestTrip(flights.trips);
                    if (nearestTrip != null) {
                        processTripInfo(nearestTrip, callback);
                    } else {
                        callback.onError(new Exception("No trips found"));
                    }
                } catch (ParseException e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private Map<String, Object> findNearestTrip(List<Map<String, Object>> trips) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Date currentDate = new Date();
        Map<String, Object> nearestTrip = null;
        long minDiff = Long.MAX_VALUE;

        for (Map<String, Object> trip : trips) {
            String tripDateStr = (String) trip.get("date");
            if (tripDateStr == null) continue;

            Date tripDate = formatter.parse(tripDateStr);
            long diff = tripDate.getTime() - currentDate.getTime();

            if (diff > 0 && diff < minDiff) {
                minDiff = diff;
                nearestTrip = trip;
            }
        }
        return nearestTrip;
    }

    private void processTripInfo(Map<String, Object> nearestTrip, TripInfoCallback callback) throws ParseException {
        AirportKey airportKey = new AirportKey();
        String airportName = (String) nearestTrip.get("airport");
        String stationCode = airportKey.airportCodes.get(airportName);

        if (stationCode == null) {
            callback.onError(new Exception("Airport code not found"));
            return;
        }

        String dateStr = (String) nearestTrip.get("date");
        SimpleDateFormat originalFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Date dateFlight = originalFormat.parse(dateStr);
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = targetFormat.format(dateFlight);

        String url = "https://api.rasp.yandex.net/v3.0/schedule/?apikey=4aec590a-e9fd-4306-ad6b-8f0bc5242f56&station=" + stationCode + "&date=" + formattedDate + "&transport_types=plane&event=departure&system=iata";
        fetchFlightSchedule(url, nearestTrip, callback);
    }

    private void fetchFlightSchedule(String url, Map<String, Object> nearestTrip, TripInfoCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                callback.onError(new Exception("Failed to fetch flight data"));
                return;
            }

            JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());
            String tripNumber = (String) nearestTrip.get("trip_number");

            if (tripNumber == null) {
                callback.onError(new Exception("Trip number not found in trip data"));
                return;
            }

            JSONArray scheduleArray = json.optJSONArray("schedule");
            if (scheduleArray != null) {
                for (int i = 0; i < scheduleArray.length(); i++) {
                    JSONObject scheduleItem = scheduleArray.getJSONObject(i);
                    JSONObject thread = scheduleItem.optJSONObject("thread");

                    if (thread != null && tripNumber.equals(thread.optString("number"))) {
                        nearestTrip.put("departure", scheduleItem.optString("departure"));
                        nearestTrip.put("terminal", scheduleItem.optString("terminal"));
                        callback.onTripInfoReady(nearestTrip);
                        return;
                    }
                }
            }
            callback.onError(new Exception("Flight with number " + tripNumber + " not found"));

        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public interface TripInfoCallback {
        void onTripInfoReady(Map<String, Object> tripInfo);
        void onError(Exception e);
    }
}