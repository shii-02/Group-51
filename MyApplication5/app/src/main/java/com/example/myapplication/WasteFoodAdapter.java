package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WasteFoodAdapter extends FoodAdapter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public WasteFoodAdapter(Context context, List<FoodItem> foodItems,
                            FoodDatabaseHelper dbHelper, RecyclerView recyclerView) {
        super(context, foodItems, dbHelper, recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);

        // Customize appearance for waste items
        holder.tvFoodName.setText(item.getName());
        holder.tvExpDate.setText(formatExpirationText(item.getExpirationDate()));

        // Style based on how long ago it expired
        styleExpiredItem(holder, item.getExpirationDate());

        // Remove edit functionality for expired items
        holder.btnEdit.setVisibility(View.GONE);

        // Change delete button color to be more prominent for waste items
        holder.btnDelete.setColorFilter(ContextCompat.getColor(context, R.color.waste_delete));

        // Add click listener for potential recovery actions
        holder.itemView.setOnLongClickListener(v -> {
            showRecoveryOptions(item, position);
            return true;
        });
    }

    private String formatExpirationText(String expirationDate) {
        try {
            Date expDate = dateFormat.parse(expirationDate);
            Date today = new Date();
            long diff = today.getTime() - expDate.getTime();
            long daysExpired = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            return String.format(Locale.getDefault(),
                    "Expired %d day%s ago",
                    daysExpired,
                    daysExpired != 1 ? "s" : "");
        } catch (ParseException e) {
            return "Expired on: " + expirationDate;
        }
    }

    private void styleExpiredItem(ViewHolder holder, String expirationDate) {
        try {
            Date expDate = dateFormat.parse(expirationDate);
            Date today = new Date();
            long diff = today.getTime() - expDate.getTime();
            long daysExpired = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            // Different styling based on how long ago it expired
            if (daysExpired <= 7) {
                // Recently expired (within a week)
                holder.itemView.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.recently_expired));
            } else if (daysExpired <= 30) {
                // Moderately expired (within a month)
                holder.itemView.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.moderately_expired));
            } else {
                // Long expired
                holder.itemView.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.long_expired));
            }

            // Strike through text for very old items
            if (daysExpired > 60) {
                holder.tvFoodName.setPaintFlags(holder.tvFoodName.getPaintFlags() |
                        Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvExpDate.setPaintFlags(holder.tvExpDate.getPaintFlags() |
                        Paint.STRIKE_THRU_TEXT_FLAG);
            }
        } catch (ParseException e) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }
    }

    private void showRecoveryOptions(FoodItem item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Expired Item Options")
                .setMessage("What would you like to do with this expired item?")
                .setPositiveButton("Mark as Composted", (dialog, which) -> {
                    markAsComposted(item, position);
                })
                .setNegativeButton("Mark as Donated", (dialog, which) -> {
                    markAsDonated(item, position);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void markAsComposted(FoodItem item, int position) {

        removeItem(position);
        Toast.makeText(context, item.getName() + " marked as composted",
                Toast.LENGTH_SHORT).show();
    }

    private void markAsDonated(FoodItem item, int position) {

        removeItem(position);
        Toast.makeText(context, item.getName() + " marked as donated",
                Toast.LENGTH_SHORT).show();
    }

    private void removeItem(int position) {
        foodItems.remove(position);
        notifyItemRemoved(position);
    }
}