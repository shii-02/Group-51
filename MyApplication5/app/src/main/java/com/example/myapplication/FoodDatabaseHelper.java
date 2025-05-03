package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FoodDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "food_inventory_db";
    private static final String TABLE_FOOD = "food_items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EXPIRATION_DATE = "expiration_date";
    private static final String COLUMN_REMINDER_DATE = "reminder_date";

    private Context context;

    public FoodDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FOOD_TABLE = "CREATE TABLE " + TABLE_FOOD + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EXPIRATION_DATE + " TEXT,"
                + COLUMN_REMINDER_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_FOOD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        onCreate(db);
    }

    public long insertFood(FoodItem food) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, food.getName());
        values.put(COLUMN_EXPIRATION_DATE, food.getExpirationDate());
        values.put(COLUMN_REMINDER_DATE, food.getReminderDate());

        long id = db.insert(TABLE_FOOD, null, values);
        db.close();
        return id;
    }

    public FoodItem getFoodItem(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOOD,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EXPIRATION_DATE, COLUMN_REMINDER_DATE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        FoodItem food = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                food = new FoodItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
            }
            cursor.close();
        }
        db.close();
        return food;
    }

    public List<FoodItem> getAllFoodItemsSortedByExpiration() {
        List<FoodItem> foodList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Modified query to handle date sorting properly
        String query = "SELECT * FROM " + TABLE_FOOD + " ORDER BY " +
                "substr(" + COLUMN_EXPIRATION_DATE + ", 7, 4) || " +
                "substr(" + COLUMN_EXPIRATION_DATE + ", 1, 2) || " +
                "substr(" + COLUMN_EXPIRATION_DATE + ", 4, 2) ASC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                FoodItem food = new FoodItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
                foodList.add(food);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return foodList;
    }

    public int updateFoodItem(FoodItem food) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, food.getName());
        values.put(COLUMN_EXPIRATION_DATE, food.getExpirationDate());
        values.put(COLUMN_REMINDER_DATE, food.getReminderDate());

        int result = db.update(TABLE_FOOD,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(food.getId())});
        db.close();
        return result;
    }

    public boolean deleteFoodItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FOOD, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> foodItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOOD, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                FoodItem item = new FoodItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
                foodItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return foodItems;
    }

    public List<FoodItem> getExpiredFoodItems() {
        List<FoodItem> expiredItems = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date today = new Date();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOOD, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String expDateStr = cursor.getString(2);
                try {
                    Date expDate = format.parse(expDateStr);
                    if (expDate.before(today)) {
                        FoodItem item = new FoodItem(
                                cursor.getLong(0),
                                cursor.getString(1),
                                expDateStr,
                                cursor.getString(3)
                        );
                        expiredItems.add(item);
                    }
                } catch (ParseException e) {
                    // Skip invalid dates
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expiredItems;
    }

    private void showImmediateNotification(String foodName) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "food_reminder_channel",
                    "Food Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "food_reminder_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Food Reminder")
                .setContentText(foodName + " is expiring today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(foodName.hashCode(), builder.build());
    }
}