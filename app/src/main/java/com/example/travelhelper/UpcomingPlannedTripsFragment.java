package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpcomingPlannedTripsFragment extends Fragment {
    // Переменные класса
    private RecyclerView recyclerView;  // Для отображения списка поездок
    private TripAdapter adapter;       // Адаптер для RecyclerView
    private List<Map<String, Object>> tripList = new ArrayList<>(); // Список поездок
    private FirebaseFirestore db;      // Для работы с Firestore
    private String userId;             // ID текущего пользователя
    private String typeOfTravel;
    private TextView Title;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Создаем View из layout-файла фрагмента
        View view = inflater.inflate(R.layout.fragment_upcoming_trips, container, false);

        userId = getArguments().getString("userId");
        typeOfTravel = getArguments().getString("typeOfTravel");
        Title = view.findViewById(R.id.UpcomingTripsText);
        if(typeOfTravel == "planned_trips") {
            Title.setText("Planned trips");
            ViewGroup.LayoutParams layoutParams = Title.getLayoutParams();
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;

            // Конвертируем 110dp в пиксели
            int marginInDp = 110; // 110dp
            float scale = getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f); // Округление

            // Устанавливаем marginStart
            marginParams.setMarginStart(marginInPx);

            // Применяем изменения
            Title.setLayoutParams(marginParams);

        }
        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();

        // Настройка RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewUpcTr);
        // Устанавливаем LinearLayoutManager (вертикальный список)
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Создаем адаптер с обработчиком кликов
        adapter = new TripAdapter(tripList, trip -> {
            // Этот код выполняется при клике на элемент списка

            // 1. Создаем новый фрагмент для деталей поездки
            TripDetailsFragment detailsFragment = new TripDetailsFragment();

            // 2. Подготавливаем данные для передачи
            Bundle args = new Bundle();
            args.putString("tripId", trip.get("id").toString()); // ID поездки
            args.putString("userId", userId); // ID пользователя
            args.putString("tripNumber", trip.get("trip_number").toString()); // Номер
            args.putString("airport", trip.get("airport").toString()); // Аэропорт
            args.putString("date", trip.get("date").toString()); // Дата

            // 3. Передаем аргументы во фрагмент
            detailsFragment.setArguments(args);

            // 4. Заменяем текущий фрагмент на фрагмент с деталями
            // Используем getParentFragmentManager() для доступа к FragmentManager активности
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentslayout, detailsFragment)
                    .addToBackStack(typeOfTravel) // Добавляем в стек возврата
                    .commit(); // Применяем транзакцию
        });

        // Устанавливаем адаптер для RecyclerView
        recyclerView.setAdapter(adapter);

        // Загружаем данные из Firestore
        loadUpcomingTrips();

        return view;
    }

    // Метод для загрузки предстоящих поездок из Firestore
    private void loadUpcomingTrips() {
        db.collection("users")
                .document(userId)
                .collection(typeOfTravel)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> trips = new ArrayList<>();
                    trips.addAll(queryDocumentSnapshots.getDocuments());

                    // Сортируем на клиенте
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

    // Парсер даты из строки (адаптируйте под ваш формат)
    private Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            return new Date(0); // Возвращаем дату по умолчанию при ошибке
        }
    }

    // Адаптер для RecyclerView
    private static class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
        private List<Map<String, Object>> trips; // Список поездок
        private final OnTripClickListener listener; // Обработчик кликов

        // Интерфейс для обработки кликов по элементам
        interface OnTripClickListener {
            void onTripClick(Map<String, Object> trip);
        }

        // Конструктор адаптера
        public TripAdapter(List<Map<String, Object>> trips, OnTripClickListener listener) {
            this.trips = trips;
            this.listener = listener;
        }

        @NonNull
        @Override
        public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Создаем View для каждого элемента списка
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trip, parent, false);
            return new TripViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
            // Получаем данные поездки для текущей позиции
            Map<String, Object> trip = trips.get(position);

            // Устанавливаем данные в View элементы
            holder.tripNumber.setText(trip.get("trip_number").toString());
            holder.airport.setText(trip.get("airport").toString());

            // Устанавливаем дату, если она есть
            if (trip.get("date") != null) {
                holder.date.setText(trip.get("date").toString());
            }

            // Обработка клика по элементу списка
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripClick(trip); // Вызываем обработчик клика
                }
            });
        }

        @Override
        public int getItemCount() {
            return trips.size(); // Возвращаем количество элементов
        }

        // ViewHolder для хранения ссылок на View элементы
        static class TripViewHolder extends RecyclerView.ViewHolder {
            TextView tripNumber, airport, date;

            public TripViewHolder(@NonNull View itemView) {
                super(itemView);
                // Находим View элементы в разметке
                tripNumber = itemView.findViewById(R.id.tripNumber);
                airport = itemView.findViewById(R.id.airport);
                date = itemView.findViewById(R.id.date);
            }
        }
    }
}