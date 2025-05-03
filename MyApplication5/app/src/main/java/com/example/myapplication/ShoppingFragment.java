package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;


public class ShoppingFragment extends Fragment {
    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private ShoppingListViewModel viewModel;
    private LinearLayout emptyContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ShoppingListViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shopping, container, false);

        emptyContainer = root.findViewById(R.id.empty_container);
        recyclerView = root.findViewById(R.id.shopping_list_recycler_view);

        // Initialize adapter
        adapter = new ShoppingListAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Set up swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new SwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton fab = root.findViewById(R.id.fab_add);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), AddIngredientActivity.class);
            addIngredientLauncher.launch(intent);
        });

        // Observe the ViewModel
        viewModel.getShoppingList().observe(getViewLifecycleOwner(), ingredients -> {
            // Let DiffUtil handle all the updates
            adapter.setIngredients(ingredients);
            updateEmptyView(ingredients.isEmpty());

            // Scroll to top when new items are added
            if (recyclerView != null && !ingredients.isEmpty()) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        return root;
    }

    private void updateEmptyView(boolean isEmpty) {
        emptyContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    void addIngredient(Ingredient newIngredient) {
        viewModel.addIngredient(newIngredient);
        checkIfEmpty();
        Toast.makeText(requireContext(), "Added: " + newIngredient.getName(), Toast.LENGTH_SHORT).show();
    }

    private void checkIfEmpty() {
        View root = getView();
        if (root != null) {
            LinearLayout emptyContainer = root.findViewById(R.id.empty_container);
            emptyContainer.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private final ActivityResultLauncher<Intent> addIngredientLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Ingredient newIngredient = (Ingredient) result.getData().getSerializableExtra("ingredient");
                    if (newIngredient != null) {
                        addIngredient(newIngredient);
                    }
                }
            }
    );

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final ShoppingListAdapter adapter;

        SwipeToDeleteCallback(ShoppingListAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Ingredient removedItem = adapter.getIngredients().get(position);

                viewModel.removeIngredient(removedItem);

                Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            viewModel.addIngredient(removedItem);
                        })
                        .show();

                checkIfEmpty();
            }
        }
    }
}