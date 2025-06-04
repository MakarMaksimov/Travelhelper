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

public class completedTravels extends Fragment {
    private RecyclerView recyclerView;
    private CompletedFlightAdapter adapter;
    private List<Map<String, String>> flightList = new ArrayList<>();
    private FlightDataSource dataSource;
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_travels, container, false);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        recyclerView = view.findViewById(R.id.recyclerViewCompletedFlights);
        dataSource = new FlightDataSource(getContext());

        setupRecyclerView();
        loadCompletedFlights();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new CompletedFlightAdapter(flightList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void loadCompletedFlights() {
        dataSource.open();
        Cursor cursor = dataSource.getFlightsByStatus("Completed");
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

    private static class CompletedFlightAdapter extends RecyclerView.Adapter<CompletedFlightAdapter.CompletedFlightViewHolder> {
        private final List<Map<String, String>> flights;

        public CompletedFlightAdapter(List<Map<String, String>> flights) {
            this.flights = flights;
        }

        @NonNull
        @Override
        public CompletedFlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_completedtrip, parent, false);
            return new CompletedFlightViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CompletedFlightViewHolder holder, int position) {
            Map<String, String> flight = flights.get(position);
            holder.flightNumber.setText(flight.get("flight_number") != null ?
                    "Рейс: " + flight.get("flight_number") : "Рейс: не указан");
            holder.airport.setText(flight.get("airport") != null ?
                    "Аэропорт: " + flight.get("airport") : "Аэропорт: не указан");
            holder.departureDate.setText(flight.get("departure_date") != null ?
                    "Дата вылета: " + flight.get("departure_date") : "Дата: не указана");

            // Добавляем проверку на null для статуса
            if (holder.status != null) {
                holder.status.setText(flight.get("status") != null ?
                        "Статус: " + flight.get("status") : "Статус: не указан");
            }
        }

        @Override
        public int getItemCount() {
            return flights.size();
        }

        static class CompletedFlightViewHolder extends RecyclerView.ViewHolder {
            TextView flightNumber, airport, departureDate, status;

            public CompletedFlightViewHolder(@NonNull View itemView) {
                super(itemView);
                flightNumber = itemView.findViewById(R.id.tripNumber);
                airport = itemView.findViewById(R.id.airport);
                departureDate = itemView.findViewById(R.id.date);
            }
        }
    }
}