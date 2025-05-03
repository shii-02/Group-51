package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;


public class ShoppingListViewModel extends ViewModel {
    private final MutableLiveData<List<Ingredient>> shoppingList = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Ingredient>> getShoppingList() {
        return shoppingList;
    }

    public void addIngredient(Ingredient ingredient) {
        List<Ingredient> currentList = shoppingList.getValue();
        if (currentList != null) {
            List<Ingredient> newList = new ArrayList<>(currentList);
            newList.add(ingredient);
            shoppingList.setValue(newList);
        }
    }

    public void removeIngredient(Ingredient ingredient) {
        List<Ingredient> currentList = shoppingList.getValue();
        if (currentList != null) {
            List<Ingredient> newList = new ArrayList<>(currentList);
            newList.remove(ingredient);
            shoppingList.setValue(newList);
        }
    }

    // Keep this method if you need it for other cases
    public void removeIngredient(int position) {
        List<Ingredient> currentList = shoppingList.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            List<Ingredient> newList = new ArrayList<>(currentList);
            newList.remove(position);
            shoppingList.setValue(newList);
        }
    }
}