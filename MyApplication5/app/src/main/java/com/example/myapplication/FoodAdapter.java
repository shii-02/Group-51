package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    protected List<FoodItem> foodItems;
    public Context context;
    private FoodDatabaseHelper databaseHelper;
    private RecyclerView recyclerView;

    public FoodAdapter(Context context, List<FoodItem> foodItems,
                       FoodDatabaseHelper dbHelper, RecyclerView recyclerView) {
        this.context = context;
        this.foodItems = foodItems;
        this.databaseHelper = dbHelper;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.food_item_layout, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvExpDate;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvExpDate = itemView.findViewById(R.id.tv_expiration_date);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    FoodItem item = foodItems.get(position);
                    openFoodDetails(item.getId());
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);
        holder.tvFoodName.setText(item.getName());
        holder.tvExpDate.setText("Expires: " + item.getExpirationDate());

        // Highlight items expiring soon
        if (isExpired(item.getExpirationDate())) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.expired));
        }
        // Check if food is expiring soon (within 3 days)
        else if (isExpiringSoon(item.getExpirationDate(), 3)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.expiring_soon));
        }
        else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.btnEdit.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                FoodItem currentItem = foodItems.get(adapterPosition);
                openEditDialog(currentItem);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            showDeleteDialog(holder.getAdapterPosition());
        });
    }

    private void openEditDialog(FoodItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_food, null);

        EditText etName = dialogView.findViewById(R.id.et_edit_name);
        EditText etExpDate = dialogView.findViewById(R.id.et_edit_exp_date);
        EditText etReminderDate = dialogView.findViewById(R.id.et_edit_reminder_date);

        etName.setText(item.getName());
        etExpDate.setText(item.getExpirationDate());
        etReminderDate.setText(item.getReminderDate() != null ? item.getReminderDate() : "");
        etExpDate.setOnClickListener(v -> showDatePicker(etExpDate));
        etReminderDate.setOnClickListener(v -> showDatePicker(etReminderDate));

        builder.setView(dialogView)
                .setTitle("Edit Food Item")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String expDate = etExpDate.getText().toString().trim();
                    String reminderDate = etReminderDate.getText().toString().trim();

                    if (name.isEmpty() || expDate.isEmpty()) {
                        Toast.makeText(context, "Name and expiration date are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    item.setName(name);
                    item.setExpirationDate(expDate);
                    item.setReminderDate(reminderDate.isEmpty() ? null : reminderDate);
                    updateFoodItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFoodItem(FoodItem item) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return databaseHelper.updateFoodItem(item) > 0;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    // Check if reminder is set for today or past
                    if (item.getReminderDate() != null && !item.getReminderDate().isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                            Date reminderDate = sdf.parse(item.getReminderDate());
                            Date today = new Date();

                            // Compare dates without time components
                            Calendar cal1 = Calendar.getInstance();
                            cal1.setTime(reminderDate);
                            cal1.set(Calendar.HOUR_OF_DAY, 0);
                            cal1.set(Calendar.MINUTE, 0);
                            cal1.set(Calendar.SECOND, 0);
                            cal1.set(Calendar.MILLISECOND, 0);

                            Calendar cal2 = Calendar.getInstance();
                            cal2.setTime(today);
                            cal2.set(Calendar.HOUR_OF_DAY, 0);
                            cal2.set(Calendar.MINUTE, 0);
                            cal2.set(Calendar.SECOND, 0);
                            cal2.set(Calendar.MILLISECOND, 0);

                            boolean isSameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
                            boolean isPast = cal1.before(cal2);

                            if (isSameDay || isPast) {
                                // Show immediate notification for today or past reminders
                                showImmediateNotification(item.getName());
                            }

                            // Always schedule the reminder (it will handle future dates)
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).scheduleReminder(
                                        item.getName(),
                                        item.getReminderDate()
                                );
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // Refresh the sorted list
                    new LoadFoodItemsTask().execute();
                    Toast.makeText(context, "Item updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void showImmediateNotification(String foodName) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "food_reminder_channel",
                    "Food Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "food_reminder_channel")
                .setSmallIcon(R.drawable.ic_notification)  // Make sure this icon exists
                .setContentTitle("Food Reminder")
                .setContentText(foodName + " is expiring today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(foodName.hashCode(), builder.build());
    }

    private class LoadFoodItemsTask extends AsyncTask<Void, Void, List<FoodItem>> {
        @Override
        protected List<FoodItem> doInBackground(Void... voids) {
            return databaseHelper.getAllFoodItemsSortedByExpiration();
        }

        @Override
        protected void onPostExecute(List<FoodItem> sortedItems) {
            foodItems.clear();
            foodItems.addAll(sortedItems);
            notifyDataSetChanged();
        }
    }


    private boolean isExpired(String expirationDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date expDate = format.parse(expirationDate);
            Date today = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            today = cal.getTime();

            return expDate.before(today);
        } catch (ParseException e) {
            return false;
        }
    }
    private boolean isExpiringSoon(String expirationDate, int daysThreshold) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date expDate = format.parse(expirationDate);
            Date today = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            today = cal.getTime();

            long diff = expDate.getTime() - today.getTime();
            long diffDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            return diffDays >= 0 && diffDays <= daysThreshold;
        } catch (ParseException e) {
            return false;
        }
    }

    private void openFoodDetails(long foodId) {
        Intent intent = new Intent(context, FoodDetailActivity.class);
        intent.putExtra("FOOD_ID", foodId);

        // Add flags if needed (for example when starting from adapter)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Food Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteItem(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(int position) {
        if (position < 0 || position >= foodItems.size()) return;

        FoodItem item = foodItems.get(position);

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return databaseHelper.deleteFoodItem(item.getId());
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    foodItems.remove(position);
                    notifyItemRemoved(position);
                    showUndoSnackbar(item, position);
                } else {
                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                }
            }
        }.execute();
    }

    private void showUndoSnackbar(FoodItem item, int position) {
        if (recyclerView != null) {
            Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> undoDelete(item, position))
                    .show();
        }
    }

    private void undoDelete(FoodItem item, int position) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return databaseHelper.insertFood(item) > 0;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    foodItems.add(position, item);
                    notifyItemInserted(position);
                }
            }
        }.execute();
    }

    public void updateData(List<FoodItem> newItems) {
        this.foodItems = newItems;
        notifyDataSetChanged();
    }

    private void showDatePicker(final EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    dateField.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}