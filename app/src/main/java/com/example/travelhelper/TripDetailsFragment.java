package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class TripDetailsFragment extends Fragment {

    private LinearLayout BackButton;
    private String userId, typeOfTravel, tripNumber, airport, date, tripId;
    private TextView tripNumberTxt, airportTxt, dateTxt;
    private UpcomingPlannedTripsFragment UpcomingPlannedTripsFr;
    private Button deleteTrip;
    private FirebaseFirestore db;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);
        UpcomingPlannedTripsFr = new UpcomingPlannedTripsFragment();
        deleteTrip = view.findViewById(R.id.deleteButtonTripDetails);
        db = FirebaseFirestore.getInstance();
        userId = getArguments().getString("userId");
        typeOfTravel = getArguments().getString("typeOfTravel");
        tripNumber = getArguments().getString("tripNumber");
        airport = getArguments().getString("airport");
        date = getArguments().getString("date");
        tripId = getArguments().getString("tripId");
        tripNumberTxt = view.findViewById(R.id.tripNumberTripDetails);
        airportTxt = view.findViewById(R.id.airportTripDetails);
        dateTxt = view.findViewById(R.id.dateTripDetails);


        tripNumberTxt.setText("     " + tripNumber);
        airportTxt.setText("     " + airport);
        dateTxt.setText("     " + date);

        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("typeOfTravel", typeOfTravel);
        UpcomingPlannedTripsFr.setArguments(args);
        BackButton = view.findViewById(R.id.backButtonContainerTripDetails);
        BackButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentslayout, UpcomingPlannedTripsFr)
                    .commit();
        });

        deleteTrip.setOnClickListener(v -> {
            FlightDataSource flightDataSource = new FlightDataSource(getContext());
            flightDataSource.open();
            long newRowId = flightDataSource.addTrip(
                    tripNumber,
                    airport,
                    date,
                    "deleted"
            );

            if (newRowId != -1) {
                db.collection("users")
                        .document(userId)
                        .collection(typeOfTravel)
                        .document(tripId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            navigateBack();
                        })
                        .addOnFailureListener(e -> {
                            flightDataSource.updateFlightStatus(newRowId, "active");
                        });
            }
            flightDataSource.close();
        });
        return view;
    }

    private void navigateBack() {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentslayout, UpcomingPlannedTripsFr)
                .commit();
    }
}