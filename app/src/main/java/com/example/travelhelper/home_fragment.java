package com.example.travelhelper;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class home_fragment extends Fragment {

    private Button UpcomingTrips, PlannedTrips;
    private UpcomingPlannedTripsFragment UpcomingTripsFr, PlannedTripsFr;

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

        return view;
    }

    private void replaceMainFragment(Fragment fr) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentslayout, fr) // ID контейнера в активности
                .addToBackStack(null) // чтобы можно было вернуться назад
                .commit();
    }

}