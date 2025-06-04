package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class add_fragment extends Fragment {

    private EditText tripnum;
    private EditText AirportName;
    private EditText DateOfFlight;
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
                String tripNumber = tripnum.getText().toString().trim();
                String airport = AirportName.getText().toString().trim();
                String dateStr = DateOfFlight.getText().toString().trim();

                if (tripNumber.isEmpty() || airport.isEmpty() || dateStr.isEmpty()) {
                    Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                sdf.setLenient(false);

                try {
                    Date flightDate = sdf.parse(dateStr);
                    long timeDiff = flightDate.getTime() - System.currentTimeMillis();

                    HashMap<String, Object> trip = new HashMap<>();
                    trip.put("trip_number", tripNumber);
                    trip.put("airport", airport);
                    trip.put("date", dateStr);
                    trip.put("timestamp", FieldValue.serverTimestamp());

                    String collectionName = (timeDiff > 648000000) ? "planned_trips" : "upcoming_trips";

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

                } catch (ParseException e) {
                    DateOfFlight.setError("Некорректный формат даты (дд-мм-гггг)");
                }
            }
        });
        return view;
    }

}