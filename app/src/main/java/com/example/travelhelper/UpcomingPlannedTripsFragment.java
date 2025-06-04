package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpcomingPlannedTripsFragment extends Fragment {
    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private List<Map<String, Object>> tripList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private String typeOfTravel;
    private TextView Title;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming_trips, container, false);

        userId = getArguments().getString("userId");
        typeOfTravel = getArguments().getString("typeOfTravel");
        Title = view.findViewById(R.id.UpcomingTripsText);
        SharedPreferences sp = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String lang = sp.getString("app_language", null);
        if("planned_trips".equals(typeOfTravel)) {
            if("en".equals(lang)) {
                Title.setText("Planned trips");
            } else if("ru".equals(lang)) {
                Title.setText("Запланированные поездки");
            }
            ViewGroup.LayoutParams layoutParams = Title.getLayoutParams();
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;

            int marginInDp = 110;
            float scale = getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);

            marginParams.setMarginStart(marginInPx);

            Title.setLayoutParams(marginParams);

        }
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewUpcTr);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripAdapter(requireContext(), tripList, trip -> {
            boolean isClosestTrip = false;
            if (!tripList.isEmpty()) {
                Map<String, Object> closestTrip = tripList.get(0);
                isClosestTrip = closestTrip.get("id").equals(trip.get("id"));
            }
            Fragment fragment;
            if (isClosestTrip && "upcoming_trips".equals(typeOfTravel)) {
                fragment = new ClosestTripFragment();
            } else {
                fragment = new TripDetailsFragment();
            }
            Bundle args = new Bundle();
            args.putString("typeOfTravel", typeOfTravel);
            args.putString("userId", userId);
            args.putString("tripNumber", trip.get("trip_number").toString());
            args.putString("airport", trip.get("airport").toString());
            args.putString("date", trip.get("date").toString());
            args.putString("tripId", trip.get("id").toString());
            fragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentslayout, fragment)
                    .commit();
        }, (trip, position) -> {
            String tripId = trip.get("id").toString();
            FlightDataSource flightDataSource = new FlightDataSource(getContext());
            try {
                flightDataSource.open();
                String flightNumber = trip.get("trip_number").toString();
                String airport = trip.get("airport").toString();
                String departureDate = trip.get("date").toString();
                long newRowId = flightDataSource.addTrip(
                        flightNumber,
                        airport,
                        departureDate,
                        "deleted"
                );

                if (newRowId != -1) {
                    db.collection("users")
                            .document(userId)
                            .collection(typeOfTravel)
                            .document(tripId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                tripList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(),
                                        "Поездка перемещена в архив",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                flightDataSource.updateFlightStatus(newRowId, "active");
                                Toast.makeText(getContext(),
                                        "Ошибка удаления: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(),
                            "Ошибка сохранения в архив",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (SQLiteException e) {
                Toast.makeText(getContext(),
                        "Ошибка базы данных: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            } finally {
                flightDataSource.close();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        loadUpcomingTrips();

        return view;
    }
    private void loadUpcomingTrips() {
        db.collection("users")
                .document(userId)
                .collection(typeOfTravel)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> trips = new ArrayList<>();
                    trips.addAll(queryDocumentSnapshots.getDocuments());
                    Collections.sort(trips, (o1, o2) -> {
                        String date1 = o1.getString("date");
                        String date2 = o2.getString("date");
                        return parseDate(date1).compareTo(parseDate(date2));
                    });

                    tripList.clear();
                    for (DocumentSnapshot doc : trips) {
                        Map<String, Object> trip = doc.getData();
                        trip.put("id", doc.getId());
                        tripList.add(trip);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    private static class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
        private final Context context;
        private List<Map<String, Object>> trips;
        private final OnTripClickListener listener;
        private final OnTripDeleteListener deleteListener;
        private static final String PREFS_NAME = "TripTasksPrefs";
        interface OnTripClickListener {
            void onTripClick(Map<String, Object> trip);
        }

        interface OnTripDeleteListener {
            void onTripDelete(Map<String, Object> trip, int position);
        }

        public TripAdapter(Context context, List<Map<String, Object>> trips,
                           OnTripClickListener listener,
                           OnTripDeleteListener deleteListener) {
            this.context = context;
            this.trips = trips;
            this.listener = listener;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trip, parent, false);
            return new TripViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
            Map<String, Object> trip = trips.get(position);
            holder.tripNumber.setText(trip.get("trip_number").toString());
            holder.airport.setText(trip.get("airport").toString());
            if (trip.get("date") != null) {
                holder.date.setText(trip.get("date").toString());
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripClick(trip);
                }
            });

            holder.menuButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v, Gravity.END);
                Context wrapper = new ContextThemeWrapper(v.getContext(), R.style.PopupMenu);
                popupMenu = new PopupMenu(wrapper, v, Gravity.END);

                popupMenu.inflate(R.menu.trip_item_menu);
                try {
                    Field[] fields = popupMenu.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popupMenu);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceShowIcon.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Confirm delete")
                                .setMessage("Are you sure?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    if (deleteListener != null) {
                                        String tripId = trip.get("id").toString();
                                        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                        prefs.edit().remove("completed_tasks_" + tripId).apply();
                                        deleteListener.onTripDelete(trip, position);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    }
                    return false;
                });

                popupMenu.show();
            });
        }

        @Override
        public int getItemCount() {
            return trips.size();
        }

        static class TripViewHolder extends RecyclerView.ViewHolder {
            TextView tripNumber, airport, date;
            ImageButton menuButton;
            public TripViewHolder(@NonNull View itemView) {
                super(itemView);
                tripNumber = itemView.findViewById(R.id.tripNumber);
                airport = itemView.findViewById(R.id.airport);
                date = itemView.findViewById(R.id.date);
                menuButton = itemView.findViewById(R.id.menu_button);
            }
        }
    }
}