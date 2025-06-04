package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ClosestTripFragment extends Fragment {

    private LinearLayout BackButton;
    private String userId, typeOfTravel, tripNumber, airport, date, tripId;
    private TextView tripNumberTxt, airportTxt, dateTxt;
    private UpcomingPlannedTripsFragment UpcomingPlannedTripsFr;
    private Button deleteTrip;
    private FirebaseFirestore db;
    private List<String> packingItems = new ArrayList<>();
    private List<String> lastMinuteItems = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_closest_trip, container, false);
        initExistingComponents(view);
        initPackingPlan(view);
        return view;
    }

    private void initExistingComponents(View view) {
        UpcomingPlannedTripsFr = new UpcomingPlannedTripsFragment();
        deleteTrip = view.findViewById(R.id.deleteButtonClosestTrip);
        db = FirebaseFirestore.getInstance();
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
            typeOfTravel = args.getString("typeOfTravel");
            tripNumber = args.getString("tripNumber");
            airport = args.getString("airport");
            date = args.getString("date");
            tripId = args.getString("tripId");
        }

        tripNumberTxt = view.findViewById(R.id.tripNumberClosestTrip);
        airportTxt = view.findViewById(R.id.airportClosestTrip);
        dateTxt = view.findViewById(R.id.dateClosestTrip);

        tripNumberTxt.setText("     " + tripNumber);
        airportTxt.setText("     " + airport);
        dateTxt.setText("     " + date);

        BackButton = view.findViewById(R.id.backButtonContainer);
        BackButton.setOnClickListener(v -> navigateBack());

        deleteTrip.setOnClickListener(v -> deleteTrip());
    }

    private void initPackingPlan(View view) {
        FrameLayout fragmentContainer = view.findViewById(R.id.closestTripFragmentContainer);
        fragmentContainer.removeAllViews();

        View packingPlanView = LayoutInflater.from(getContext()).inflate(R.layout.packing_plan_layout, fragmentContainer, false);
        fragmentContainer.addView(packingPlanView);

        LinearLayout planContainer = packingPlanView.findViewById(R.id.planContainer);
        LinearLayout packingInputContainer = packingPlanView.findViewById(R.id.packingInputContainer);
        LinearLayout lastMinuteInputContainer = packingPlanView.findViewById(R.id.lastMinuteInputContainer);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout packingItemsContainer = packingPlanView.findViewById(R.id.packingItemsContainer);
        EditText packingItemsInput = packingPlanView.findViewById(R.id.packingItemsInput);
        EditText lastMinuteItemsInput = packingPlanView.findViewById(R.id.lastMinuteItemsInput);
        Button addPackingItemBtn = packingPlanView.findViewById(R.id.addPackingItemBtn);
        Button addLastMinuteItemBtn = packingPlanView.findViewById(R.id.addLastMinuteItemBtn);

        if (packingInputContainer.getParent() != null) {
            ((ViewGroup)packingInputContainer.getParent()).removeView(packingInputContainer);
        }
        if (lastMinuteInputContainer.getParent() != null) {
            ((ViewGroup)lastMinuteInputContainer.getParent()).removeView(lastMinuteInputContainer);
        }
        if (packingItemsContainer.getParent() != null) {
            ((ViewGroup)packingItemsContainer.getParent()).removeView(packingItemsContainer);
        }

        packingInputContainer.setVisibility(View.GONE);
        lastMinuteInputContainer.setVisibility(View.GONE);
        packingItemsContainer.setVisibility(View.GONE);

        List<TaskItem> tasks = createTasks();
        List<View> taskViews = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            TaskItem task = tasks.get(i);
            View taskView = createTaskView(task, i);
            planContainer.addView(taskView);
            taskViews.add(taskView);

            if (task.getText().contains("Введите вещи") || task.getText().contains("Enter the things")) {
                planContainer.addView(packingInputContainer);
                packingInputContainer.setVisibility(View.VISIBLE);
            } else if (task.getText().contains("Доложите всё") || task.getText().contains("Add up everything")) {
                planContainer.addView(packingItemsContainer);
                packingItemsContainer.setVisibility(View.VISIBLE);
            }
        }

        addPackingItemBtn.setOnClickListener(v -> {
            String item = packingItemsInput.getText().toString().trim();
            if (!item.isEmpty()) {
                packingItems.add(item);
                packingItemsInput.setText("");
                Toast.makeText(getContext(), "Вещь добавлена в список: " + item, Toast.LENGTH_SHORT).show();
                updatePackingItemsList(packingItemsContainer);
            }
        });

        addLastMinuteItemBtn.setOnClickListener(v -> {
            String item = lastMinuteItemsInput.getText().toString().trim();
            if (!item.isEmpty()) {
                lastMinuteItems.add(item);
                lastMinuteItemsInput.setText("");
                Toast.makeText(getContext(), "Вещь добавлена в список: " + item, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePackingItemsList(LinearLayout container) {
        container.removeAllViews();

        if (packingItems.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("Список вещей пуст");
            container.addView(emptyText);
        } else {
            for (String item : packingItems) {
                TextView itemView = new TextView(getContext());
                itemView.setText("• " + item);
                container.addView(itemView);
            }
        }
    }

    private List<TaskItem> createTasks() {
        List<TaskItem> tasks = new ArrayList<>();
        String currentLang = requireContext()
                .getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                .getString("app_language", Locale.getDefault().getLanguage());
        if(currentLang.equals("ru")) {
            tasks.add(new TaskItem("1-й Этап - Вещи:", true));
            tasks.add(new TaskItem("Соберите чемодан!", false));
            tasks.add(new TaskItem("Проверьте размер и вес ручной клади и багажа!", false));
            tasks.add(new TaskItem("Не забудьте сложить все важные документы!", false));
            tasks.add(new TaskItem("Введите вещи, которые нужно будет доложить перед поездкой:", false));

            tasks.add(new TaskItem("2-й Этап - Финансы:", true));
            tasks.add(new TaskItem("Не забудьте взять с собой наличные!", false));
            tasks.add(new TaskItem("Обменяйте валюту!", false));
            tasks.add(new TaskItem("Проверьте, работает ли карта в месте отдыха!", false));

            tasks.add(new TaskItem("3-й Этап - Мобильная связь:", true));
            tasks.add(new TaskItem("Узнайте, как работает роуминг в месте отдыха!", false));

            tasks.add(new TaskItem("4-й Этап - Последние приготовления:", true));
            tasks.add(new TaskItem("Проверьте все важные документы!", false));
            tasks.add(new TaskItem("Доложите всё, что хотели:", false));
            tasks.add(new TaskItem("Вызовите такси!", false));
        }
        else{
            tasks.add(new TaskItem("Stage 1 - Things:", true));
            tasks.add(new TaskItem("Pack your suitcase!", false));
            tasks.add(new TaskItem("Check the size and weight of hand luggage and luggage!", false));
            tasks.add(new TaskItem("Don't forget to pack all the important documents!", false));
            tasks.add(new TaskItem("Enter the things that will need to be reported before the trip:", false));

            tasks.add(new TaskItem("Stage 2 - Finance:", true));
            tasks.add(new TaskItem("Don't forget to bring cash with you!", false));
            tasks.add(new TaskItem("Exchange the currency!", false));
            tasks.add(new TaskItem("Check if the card is working at your vacation spot!", false));

            tasks.add(new TaskItem("Stage 3 - Mobile communication:", true));
            tasks.add(new TaskItem("Find out how roaming works at your vacation destination!", false));

            tasks.add(new TaskItem("Stage 4 - Final preparations:", true));
            tasks.add(new TaskItem("Check all the important documents!", false));
            tasks.add(new TaskItem("Add up everything you wanted:", false));
            tasks.add(new TaskItem("Call a taxi!", false));
        }

        return tasks;
    }

    private View createTaskView(TaskItem task, int position) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (task.isHeader()) {
            TextView header = (TextView) inflater.inflate(R.layout.task_header_layout, null);
            header.setText(task.getText());
            return header;
        } else {
            LinearLayout taskLayout = (LinearLayout) inflater.inflate(R.layout.task_item_layout, null);
            CheckBox checkBox = taskLayout.findViewById(R.id.taskCheckBox);
            TextView taskText = taskLayout.findViewById(R.id.taskText);

            taskText.setText(task.getText());
            checkBox.setChecked(false);

            taskLayout.setTag(position);

            boolean isFirstTask = position == 1;
            if (!isFirstTask) {
                taskLayout.setAlpha(0.5f);
                checkBox.setEnabled(false);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && getView() != null) {
                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(true);
                    checkBox.setOnCheckedChangeListener((b, checked) -> {
                        if (!checked) {
                            checkBox.setChecked(true);
                        }
                    });

                    unlockNextTask(position);
                }
            });

            return taskLayout;
        }
    }

    private void unlockNextTask(int currentPosition) {
        FrameLayout fragmentContainer = getView().findViewById(R.id.closestTripFragmentContainer);
        if (fragmentContainer == null) return;

        LinearLayout planContainer = fragmentContainer.findViewById(R.id.planContainer);
        if (planContainer == null) return;
        for (int i = 0; i < planContainer.getChildCount(); i++) {
            View child = planContainer.getChildAt(i);
            if (child.getTag() != null && child.getTag() instanceof Integer) {
                int taskPosition = (Integer) child.getTag();
                if (taskPosition > currentPosition) {
                    if (child instanceof LinearLayout) {
                        CheckBox nextCheckBox = child.findViewById(R.id.taskCheckBox);
                        if (nextCheckBox != null) {
                            child.setAlpha(1.0f);
                            nextCheckBox.setEnabled(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private View findViewWithTag(String text) {
        FrameLayout fragmentContainer = getView().findViewById(R.id.closestTripFragmentContainer);
        return fragmentContainer.findViewWithTag(text);
    }

    private void deleteTrip() {
        if (getContext() == null) return;

        FlightDataSource flightDataSource = new FlightDataSource(getContext());
        try {
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
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                navigateBack();
                            } else {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Ошибка при удалении: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                                flightDataSource.updateFlightStatus(newRowId, "active");
                            }
                            flightDataSource.close();
                        });
            } else {
                flightDataSource.close();
            }
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (flightDataSource != null) {
                flightDataSource.close();
            }
        }
    }

    private void navigateBack() {
        if (getActivity() == null) return;

        UpcomingPlannedTripsFragment fragment = new UpcomingPlannedTripsFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("typeOfTravel", typeOfTravel);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentslayout, fragment)
                .commit();
    }

    private static class TaskItem {
        private final String text;
        private final boolean header;
        private final boolean inputPrompt;

        public TaskItem(String text, boolean isHeader) {
            this.text = text;
            this.header = isHeader;
            this.inputPrompt = text.contains("Введите вещи") || text.contains("Доложите всё");
        }

        public String getText() {
            return text;
        }

        public boolean isHeader() {
            return header;
        }

        public boolean isInputPrompt() {
            return inputPrompt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TaskItem taskItem = (TaskItem) o;
            return text.equals(taskItem.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }
    }

}
