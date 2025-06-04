package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeletedFlightsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FlightAdapter adapter;
    private List<Map<String, String>> flightList = new ArrayList<>();
    private FlightDataSource dataSource;
    private FirebaseFirestore db;
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deleted_flights, container, false);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        recyclerView = view.findViewById(R.id.recyclerViewDeletedTr);
        db = FirebaseFirestore.getInstance();
        dataSource = new FlightDataSource(getContext());

        setupRecyclerView();
        loadDeletedFlights();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FlightAdapter(flightList, this::showPopupMenu);
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

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.deleted_flight_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_restore) {
                restoreTrip(position);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void restoreTrip(int position) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Ошибка: пользователь не идентифицирован", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> flight = flightList.get(position);
        String flightId = flight.get("id");

        HashMap<String, Object> trip = new HashMap<>();
        trip.put("trip_number", flight.get("flight_number"));
        trip.put("airport", flight.get("airport"));
        trip.put("date", flight.get("departure_date"));
        trip.put("timestamp", Timestamp.now());
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            sdf.setLenient(false);
            Date flightDate = sdf.parse(flight.get("departure_date"));
            long timeDiff = flightDate.getTime() - System.currentTimeMillis();
            if (timeDiff > 0) {
                String collectionName = " ";
                collectionName = (timeDiff > 648000000) ? "planned_trips" : "upcoming_trips";

                db.collection("users")
                        .document(userId)
                        .collection(collectionName)
                        .add(trip)
                        .addOnSuccessListener(documentReference -> {
                            dataSource.open();
                            dataSource.deleteFlight(Long.parseLong(flightId));
                            dataSource.close();

                            flightList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Поездка восстановлена", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Ошибка при восстановлении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                dataSource.open();
                boolean updated = dataSource.updateFlightStatus(
                        Long.parseLong(flightId),
                        "Completed");
                dataSource.close();

                if (updated) {
                    flight.put("status", "Completed");
                    flightList.remove(position);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Статус поездки обновлен на 'Completed'", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ошибка обновления статуса", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка формата даты", Toast.LENGTH_SHORT).show();
        }
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
        private final OnMenuButtonClickListener menuButtonClickListener;

        public FlightAdapter(List<Map<String, String>> flights, OnMenuButtonClickListener listener) {
            this.flights = flights;
            this.menuButtonClickListener = listener;
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

            holder.menuButton.setOnClickListener(v -> {
                menuButtonClickListener.onMenuButtonClick(v, position);
            });
        }

        @Override
        public int getItemCount() {
            return flights.size();
        }

        static class FlightViewHolder extends RecyclerView.ViewHolder {
            TextView flightNumber, airport, departureDate;
            View menuButton;

            public FlightViewHolder(@NonNull View itemView) {
                super(itemView);
                flightNumber = itemView.findViewById(R.id.tripNumber);
                airport = itemView.findViewById(R.id.airport);
                departureDate = itemView.findViewById(R.id.date);
                menuButton = itemView.findViewById(R.id.menu_button);
            }
        }

        interface OnMenuButtonClickListener {
            void onMenuButtonClick(View view, int position);
        }
    }
}