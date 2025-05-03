package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;


public class AddIngredientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(view -> {
            if (validateInput()) {
                Ingredient ingredient = createIngredientFromInput();
                returnIngredient(ingredient);
            }
        });
    }

    private boolean validateInput() {
        TextInputEditText nameInput = findViewById(R.id.ingredient_name_input);
        TextInputEditText quantityInput = findViewById(R.id.quantity_input);

        // Null value checking
        if (TextUtils.isEmpty(nameInput.getText())) {
            nameInput.setError("Please enter an ingredient name");
            return false;
        }

        if (TextUtils.isEmpty(quantityInput.getText())) {
            quantityInput.setError("Please enter a quantity");
            return false;
        }

        return true;
    }

    private Ingredient createIngredientFromInput() {
        TextInputEditText nameInput = findViewById(R.id.ingredient_name_input);
        TextInputEditText quantityInput = findViewById(R.id.quantity_input);
        RadioGroup priorityGroup = findViewById(R.id.priority_group);

        String priority = "Medium";
        int selectedId = priorityGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.priority_high) {
            priority = "High";
        } else if (selectedId == R.id.priority_low) {
            priority = "Low";
        }

        // Safe numeric conversion
        int quantity = 0;
        try {
            quantity = Integer.parseInt(quantityInput.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
        }

        return new Ingredient(
                nameInput.getText().toString().trim(),
                quantity,
                priority
        );
    }

    private void returnIngredient(Ingredient ingredient) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("ingredient", ingredient);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}