package com.example.travelhelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class home_fragment extends Fragment {

    private Button UpcomingTrips, PlannedTrips;
    private UpcomingTripsFragment UpcomingTripsFr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_fragment, container, false);

        UpcomingTrips = view.findViewById(R.id.Upcomingtrbutton);
        PlannedTrips = view.findViewById(R.id.Plannedtrbutton);
        UpcomingTripsFr = new UpcomingTripsFragment();

        UpcomingTrips.setOnClickListener(v -> {
            if (getActivity() instanceof FragmentNavigation) {
                ((FragmentNavigation) getActivity()).navigateTo(new UpcomingTripsFragment());
            }
        });

        PlannedTrips.setOnClickListener(v -> {
            if (getActivity() instanceof FragmentNavigation) {
                ((FragmentNavigation) getActivity()).navigateTo(new PlannedTripsFragment());
            }
        });
        return view;
    }

    public interface FragmentNavigation {
        void navigateTo(Fragment fragment);
    }

}