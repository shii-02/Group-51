package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ViewFoodFragment extends Fragment {
    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private FoodDatabaseHelper databaseHelper;
    private TextView tvEmptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new FoodDatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_food, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmptyView = view.findViewById(R.id.tv_empty_view);

        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new FoodAdapter(requireContext(), new ArrayList<>(), databaseHelper, recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        refreshData();
    }

    public void refreshData() {
        new AsyncTask<Void, Void, List<FoodItem>>() {
            @Override
            protected List<FoodItem> doInBackground(Void... voids) {
                try {
                    return databaseHelper.getAllFoodItemsSortedByExpiration();
                } catch (Exception e) {
                    Log.e("ViewFoodFragment", "Error loading data", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<FoodItem> foodItems) {
                if (foodItems != null) {
                    adapter.updateData(foodItems);
                    updateEmptyView(foodItems.isEmpty());

                    // Optional: Show toast if any items are expiring soon
                    if (hasExpiringSoonItems(foodItems)) {
                        Toast.makeText(requireContext(),
                                "Some items are expiring soon!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(),
                            "Error loading food items",
                            Toast.LENGTH_SHORT).show();
                    updateEmptyView(true);
                }
            }
        }.execute();
    }

    private boolean hasExpiringSoonItems(List<FoodItem> items) {
        for (FoodItem item : items) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                Date expDate = format.parse(item.getExpirationDate());
                Date today = new Date();

                long diff = expDate.getTime() - today.getTime();
                long diffDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                if (diffDays >= 0 && diffDays <= 3) {
                    return true;
                }
            } catch (ParseException e) {
            }
        }
        return false;
    }

    private void updateEmptyView(boolean isEmpty) {
        if (getView() != null) {
            tvEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }
}