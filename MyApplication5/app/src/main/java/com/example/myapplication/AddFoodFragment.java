package com.example.myapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddFoodFragment extends Fragment {

    private EditText etFoodName, etExpirationDate, etReminderDate;
    private Button btnSave, btnCancel, btnViewAll;
    private FoodDatabaseHelper databaseHelper;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    public AddFoodFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_food, container, false);

        etFoodName = view.findViewById(R.id.et_food_name);
        etExpirationDate = view.findViewById(R.id.et_expiration_date);
        etReminderDate = view.findViewById(R.id.et_reminder_date);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnViewAll = view.findViewById(R.id.btn_view_all);

        databaseHelper = new FoodDatabaseHelper(getActivity());

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);


        setDateFieldListeners();


        btnSave.setOnClickListener(v -> {
            try {
                saveFood();
            } catch (Exception e) {
                Log.e("AddFoodFragment", "Save error", e);
                Toast.makeText(requireContext(), "Error saving food", Toast.LENGTH_SHORT).show();
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

;
        btnViewAll.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity instanceof MainActivity && isAdded()) {
                ((MainActivity) activity).switchToViewFood();
            }
        });

        return view;
    }

    private void setDateFieldListeners() {
        etExpirationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etExpirationDate);
            }
        });

        etReminderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etReminderDate);
            }
        });
    }

    private void showDatePicker(final EditText dateField) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateField.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveFood() {
        String foodName = etFoodName.getText().toString().trim();
        String expirationDate = etExpirationDate.getText().toString().trim();
        String reminderDate = etReminderDate.getText().toString().trim();

        if (foodName.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a food name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (expirationDate.isEmpty()) {
            Toast.makeText(getActivity(), "Please select an expiration date", Toast.LENGTH_SHORT).show();
            return;
        }

        FoodItem foodItem = new FoodItem(foodName, expirationDate, reminderDate);

        long id = databaseHelper.insertFood(foodItem);

        if (id > 0) {
            scheduleReminder(foodItem.getName(), foodItem.getReminderDate());
            Toast.makeText(getActivity(), "Food saved successfully", Toast.LENGTH_SHORT).show();
            clearFields();

            ((MainActivity) requireActivity()).refreshFoodList();
        } else {
            Toast.makeText(getActivity(), "Failed to save food", Toast.LENGTH_SHORT).show();
        }
    }
    private void scheduleReminder(String foodName, String reminderDate) {
        if (reminderDate == null || reminderDate.isEmpty()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date date = sdf.parse(reminderDate);

            // If reminder date is today or in the past, show immediately
            if (date == null || !date.after(new Date())) {
                showNotificationNow(foodName);
                return;
            }

            Intent intent = new Intent(requireContext(), ReminderReceiver.class);
            intent.putExtra("FOOD_NAME", foodName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    foodName.hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        date.getTime(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        date.getTime(),
                        pendingIntent
                );
            }
        } catch (Exception e) {
            Log.e("Alarm", "Scheduling failed", e);
            Toast.makeText(requireContext(), "Failed to set reminder", Toast.LENGTH_SHORT).show();
        }
    }
    private String formatDateForStorage(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            SimpleDateFormat storageFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = displayFormat.parse(displayDate);
            return storageFormat.format(date);
        } catch (ParseException e) {
            return displayDate; // fallback
        }
    }

    private void showNotificationNow(String foodName) {
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        intent.putExtra("FOOD_NAME", foodName);
        requireContext().sendBroadcast(intent);
    }


    private void clearFields() {
        etFoodName.setText("");
        etExpirationDate.setText("");
        etReminderDate.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabVisibility(true);
        }
    }
}
