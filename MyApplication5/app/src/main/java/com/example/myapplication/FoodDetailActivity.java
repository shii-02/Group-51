package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FoodDetailActivity extends AppCompatActivity {
    private TextView tvFoodName, tvExpirationDate, tvReminderDate;
    private Button btnBack;
    private FoodDatabaseHelper databaseHelper;
    private long foodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        tvFoodName = findViewById(R.id.tv_detail_food_name);
        tvExpirationDate = findViewById(R.id.tv_detail_expiration_date);
        tvReminderDate = findViewById(R.id.tv_detail_reminder_date);
        btnBack = findViewById(R.id.btn_back);
        databaseHelper = new FoodDatabaseHelper(this);


        foodId = getIntent().getLongExtra("FOOD_ID", -1);

        if (foodId != -1) {
            loadFoodDetails();
        } else {
            Toast.makeText(this, "Error: Food item not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadFoodDetails() {
        FoodItem foodItem = databaseHelper.getFoodItem(foodId);

        if (foodItem != null) {
            tvFoodName.setText(foodItem.getName());
            tvExpirationDate.setText("Expiration Date: " + foodItem.getExpirationDate());

            if (foodItem.getReminderDate() != null && !foodItem.getReminderDate().isEmpty()) {
                tvReminderDate.setText("Reminder Date: " + foodItem.getReminderDate());
                tvReminderDate.setVisibility(View.VISIBLE);
            } else {
                tvReminderDate.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Error loading food details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}