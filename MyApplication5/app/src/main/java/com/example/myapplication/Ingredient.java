package com.example.myapplication;

import java.io.Serializable;
import java.util.Objects;

public class Ingredient implements Serializable {
    private String name;
    private int quantity;
    private String priority;
    private boolean purchased;

    public Ingredient(String name, int quantity, String priority) {
        this.name = name;
        this.quantity = quantity;
        this.priority = priority;
        this.purchased = false;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return quantity == that.quantity &&
                purchased == that.purchased &&
                name.equals(that.name) &&
                priority.equals(that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, priority, purchased);
    }
}