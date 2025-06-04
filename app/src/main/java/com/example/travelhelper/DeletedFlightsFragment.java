package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeletedFlightsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FlightAdapter adapter;
    private List<Map<String, String>> flightList = new ArrayList<>();
    private FlightDataSource dataSource;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deleted_flights, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDeletedTr);

        dataSource = new FlightDataSource(getContext());

        setupRecyclerView();
        loadDeletedFlights();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FlightAdapter(flightList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void loadDeletedFlights() {
        dataSource.open();
        Cursor cursor = dataSource.getFlightsByStatus("deleted");
        flightList.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Map<String, String> flight = new HashMap<>();
                flight.put("id", cursor.getString(cursor.getColumnIndexOrThrow(FlightDatabaseHelper.COLUMN_ID)));
                flight.put("flight_number", cursor.getString(cursor.getColumnIndexOrThrow(FlightDatabaseHelper.COLUMN_FLIGHT_NUMBER)));
                flight.put("airport", cursor.getString(cursor.getColumnIndexOrThrow(FlightDatabaseHelper.COLUMN_AIRPORT)));
                flight.put("departure_date", cursor.getString(cursor.getColumnIndexOrThrow(FlightDatabaseHelper.COLUMN_DEPARTURE_DATE)));
                flight.put("status", cursor.getString(cursor.getColumnIndexOrThrow(FlightDatabaseHelper.COLUMN_STATUS)));

                flightList.add(flight);
            } while (cursor.moveToNext());
            cursor.close();
        }

        dataSource.close();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {
        private final List<Map<String, String>> flights;

        public FlightAdapter(List<Map<String, String>> flights) {
            this.flights = flights;
        }

        @NonNull
        @Override
        public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_deleted_trip, parent, false);
            return new FlightViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
            Map<String, String> flight = flights.get(position);
            holder.flightNumber.setText("Рейс: " + flight.get("flight_number"));
            holder.airport.setText("Аэропорт: " + flight.get("airport"));
            holder.departureDate.setText("Дата вылета: " + flight.get("departure_date"));
        }

        @Override
        public int getItemCount() {
            return flights.size();
        }

        static class FlightViewHolder extends RecyclerView.ViewHolder {
            TextView flightNumber, airport, departureDate, status;

            public FlightViewHolder(@NonNull View itemView) {
                super(itemView);
                flightNumber = itemView.findViewById(R.id.tripNumber);
                airport = itemView.findViewById(R.id.airport);
                departureDate = itemView.findViewById(R.id.date);;
                //menuButton = itemView.findViewById(R.id.menu_button);
            }
        }
    }
}