package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class add_fragment extends Fragment {

    private EditText tripnum;
    private EditText AirportName;
    private DatePicker DateOfFlight;
    private Button AddTripButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("AddFragment", "Fragment created");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_fragment, container, false);
        tripnum = view.findViewById(R.id.tripnum);
        AirportName = view.findViewById(R.id.Airportname);
        DateOfFlight = view.findViewById(R.id.DateofFlight);
        AddTripButton = view.findViewById(R.id.addtripbutton);

        if (getArguments() == null || getArguments().getString("userId") == null) {
            Toast.makeText(getActivity(), "Ошибка: ID пользователя не получен", Toast.LENGTH_SHORT).show();
            return view;
        }
        String userId = getArguments().getString("userId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AddTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch alarmSwitch = view.findViewById(R.id.alarmSwitch);
                boolean isAlarmEnabled = alarmSwitch.isChecked();
                String tripNumber = tripnum.getText().toString().trim();
                String airport = AirportName.getText().toString().trim();
                int day = DateOfFlight.getDayOfMonth();
                int month = DateOfFlight.getMonth();
                int year = DateOfFlight.getYear();

                String alarm_clock = "";
                if(isAlarmEnabled)
                    alarm_clock = "true_" + tripNumber;
                else
                    alarm_clock = "false_" + tripNumber;
                SharedPreferences sp = requireContext().getSharedPreferences("App_Prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("alarm_clock", alarm_clock);
                editor.apply();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                String dateStr = sdf.format(calendar.getTime());


                if (tripNumber.isEmpty() || airport.isEmpty() || dateStr.isEmpty()) {
                    Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
                sdf2.setLenient(false);

                try {
                    Date flightDate = sdf2.parse(dateStr);
                    long timeDiff = flightDate.getTime() - System.currentTimeMillis();

                    HashMap<String, Object> trip = new HashMap<>();
                    trip.put("trip_number", tripNumber);
                    trip.put("airport", airport);
                    trip.put("date", dateStr);
                    trip.put("timestamp", FieldValue.serverTimestamp());

                    String collectionName = " ";
                            if(timeDiff > 648000000) {
                                collectionName = "planned_trips";
                                db.collection("users")
                                        .document(userId)
                                        .collection(collectionName)
                                        .add(trip)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(getContext(), "Поездка добавлена!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {


                                            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            }
                            else{
                                if(timeDiff > 0) {
                                    collectionName = "upcoming_trips";
                                    db.collection("users")
                                            .document(userId)
                                            .collection(collectionName)
                                            .add(trip)
                                            .addOnSuccessListener(documentReference -> {
                                                Toast.makeText(getContext(), "Поездка добавлена!", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {


                                                Toast.makeText(getContext(), "Ошибка: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                }
                                else
                                   Toast.makeText(getContext(), "Wrong date!", Toast.LENGTH_LONG).show();
                            }



                } catch (ParseException e) {
                    Toast.makeText(getContext(), "Wrong date!", Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

}