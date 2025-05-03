package com.example.myapplication;

public class FoodItem {
    private long id;
    private String name;
    private String expirationDate;
    private String reminderDate;

    public FoodItem() {
    }

    public FoodItem(String name, String expirationDate, String reminderDate) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.reminderDate = reminderDate;
    }

    public FoodItem(long id, String name, String expirationDate, String reminderDate) {
        this.id = id;
        this.name = name;
        this.expirationDate = expirationDate;
        this.reminderDate = reminderDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }
}