package com.example.travelhelper;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class home_fragment extends Fragment {

    private MaterialCardView UpcomingTrips, PlannedTrips, DeletedTrips, CompletedTrips;
    private UpcomingPlannedTripsFragment UpcomingTripsFr, PlannedTripsFr;
    private DeletedFlightsFragment DeletedFlightsFr;
    private completedTravels completedTravelsFr;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_fragment, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        } else {
            Log.e("HomeFragment", "No arguments bundle!");
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return view;
        }
        UpcomingTrips = view.findViewById(R.id.Upcomingtrbutton);
        PlannedTrips = view.findViewById(R.id.Plannedtrbutton);
        DeletedTrips = view.findViewById(R.id.Deletedtrbutton);
        CompletedTrips = view.findViewById(R.id.Completedtrbutton);
        UpcomingTrips.setOnClickListener(v -> {
            UpcomingTripsFr = new UpcomingPlannedTripsFragment();
            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putString("typeOfTravel", "upcoming_trips");
            UpcomingTripsFr.setArguments(args);
            replaceMainFragment(UpcomingTripsFr);
        });

        PlannedTrips.setOnClickListener(v -> {
            PlannedTripsFr = new UpcomingPlannedTripsFragment();
            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putString("typeOfTravel", "planned_trips");
            PlannedTripsFr.setArguments(args);
            replaceMainFragment(PlannedTripsFr);
        });

        DeletedFlightsFr = new DeletedFlightsFragment();
        DeletedTrips.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putString("typeOfTravel", "upcoming_trips");
            DeletedFlightsFr.setArguments(args);
            replaceMainFragment(DeletedFlightsFr);
        });

        completedTravelsFr = new completedTravels();
        CompletedTrips.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putString("typeOfTravel", "upcoming_trips");
            completedTravelsFr.setArguments(args);
            replaceMainFragment(completedTravelsFr);
        });
        return view;
    }

    private void replaceMainFragment(Fragment fr) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentslayout, fr)
                .addToBackStack(null)
                .commit();
    }

}