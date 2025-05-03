package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class WasteFragment extends Fragment {
    private RecyclerView recyclerView;
    private WasteFoodAdapter adapter;
    private FoodDatabaseHelper databaseHelper;
    private TextView tvEmptyView;
    private TextView tvWasteCount;
    private TextView tvWasteStats;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new FoodDatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waste, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmptyView = view.findViewById(R.id.tv_empty_view);
        tvWasteCount = view.findViewById(R.id.tv_waste_count);
        tvWasteStats = view.findViewById(R.id.tv_waste_stats);

        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new WasteFoodAdapter(requireContext(), new ArrayList<>(), databaseHelper, recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        refreshData();
    }

    public void refreshData() {
        new AsyncTask<Void, Void, List<FoodItem>>() {
            @Override
            protected List<FoodItem> doInBackground(Void... voids) {
                return databaseHelper.getExpiredFoodItems();
            }

            @Override
            protected void onPostExecute(List<FoodItem> expiredItems) {
                adapter.updateData(expiredItems);
                updateEmptyView(expiredItems.isEmpty());
                updateWasteStatistics(expiredItems);
            }
        }.execute();
    }

    private void updateWasteStatistics(List<FoodItem> expiredItems) {
        int count = expiredItems.size();
        tvWasteCount.setText(getString(R.string.waste_count, count));

//        // Calculate potential cost (example: $5 per item)
//        double estimatedCost = count * 5.0;
//        tvWasteStats.setText(getString(R.string.waste_stats, count, estimatedCost));
    }

    private void updateEmptyView(boolean isEmpty) {
        if (getView() != null) {
            tvEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            tvWasteCount.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            tvWasteStats.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }
}