package com.example.travelhelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class home_fragment extends Fragment {

    private Button UpcomingTrips;
    private Button PlannedTrips;
    private Fragment UpcomingTripsFr;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_fragment, container);
        // Inflate the layout for this fragment

        UpcomingTrips = view.findViewById(R.id.Upcomingtrbutton);
        PlannedTrips = view.findViewById(R.id.Plannedtrbutton);
        UpcomingTripsFr = new UpcomingTripsFragment();
        UpcomingTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(UpcomingTripsFr);
            }
        });
        return inflater.inflate(R.layout.fragment_home_fragment, container, false);
    }

    private void setNewFragment(Fragment fr){
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentslayout, fr);
        ft.commit();
    }

}