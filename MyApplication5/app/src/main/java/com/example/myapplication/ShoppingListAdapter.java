package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;


public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final Context context;
    private OnItemClickListener listener;
    private RecyclerView recyclerView;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public ShoppingListAdapter(Context context) {
        this.context = context;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setIngredients(List<Ingredient> newIngredients) {
        synchronized (this) {
            List<Ingredient> oldList = new ArrayList<>(this.ingredients);
            this.ingredients.clear();
            this.ingredients.addAll(newIngredients);
            sortIngredients();
            calculateDiffAndDispatch(oldList, this.ingredients);
        }
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    private void sortIngredients() {
        synchronized (this) {
            ingredients.sort((o1, o2) -> {
                if (o1.isPurchased() != o2.isPurchased()) {
                    return o1.isPurchased() ? 1 : -1;
                }
                return getPriorityWeight(o2) - getPriorityWeight(o1);
            });
        }
    }

    private int getPriorityWeight(Ingredient item) {
        switch(item.getPriority()) {
            case "High": return 3;
            case "Medium": return 2;
            default: return 1; // Low
        }
    }

    private void calculateDiffAndDispatch(List<Ingredient> oldList, List<Ingredient> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Ingredient oldItem = oldList.get(oldItemPosition);
                Ingredient newItem = newList.get(newItemPosition);
                return oldItem.isPurchased() == newItem.isPurchased() &&
                        oldItem.getPriority().equals(newItem.getPriority());
            }
        });

        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping_ingredient, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        Log.d("ShoppingAdapter", "Binding position " + position + ": " + ingredient.getName());

        holder.checkBox.setOnCheckedChangeListener(null);

        holder.ingredientName.setText(ingredient.getName());
        holder.ingredientQuantity.setText(
                String.format(context.getString(R.string.quantity_prefix), ingredient.getQuantity())
        );

        // Set priority icon
        int priorityIconRes;
        int priorityColor;
        switch(ingredient.getPriority()) {
            case "High":
                priorityIconRes = R.drawable.ic_priority_high;
                priorityColor = ContextCompat.getColor(context, R.color.priority_high);
                break;
            case "Medium":
                priorityIconRes = R.drawable.ic_priority_medium;
                priorityColor = ContextCompat.getColor(context, R.color.priority_medium);
                break;
            default:
                priorityIconRes = R.drawable.ic_priority_low;
                priorityColor = ContextCompat.getColor(context, R.color.priority_low);
        }
        holder.priorityIcon.setImageResource(priorityIconRes);
        holder.priorityIcon.setColorFilter(priorityColor);

        // Set checkbox state
        holder.checkBox.setChecked(ingredient.isPurchased());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int currentPos = holder.getAbsoluteAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            ingredients.get(currentPos).setPurchased(isChecked);

            List<Ingredient> oldList = new ArrayList<>(ingredients);
            sortIngredients();
            calculateDiffAndDispatch(oldList, ingredients);
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView ingredientName;
        public TextView ingredientQuantity;
        public ImageView priorityIcon;
        public CheckBox checkBox;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientQuantity = itemView.findViewById(R.id.ingredient_quantity);
            priorityIcon = itemView.findViewById(R.id.priority_icon);
            checkBox = itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(position);
                    }
                }
                return true;
            });
        }
    }
}