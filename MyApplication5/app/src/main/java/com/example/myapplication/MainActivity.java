package com.example.myapplication;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ViewFoodFragment viewFoodFragment;
    private ShoppingFragment shoppingFragment;
    private WasteFragment wasteFragment;
    private SettingsFragment settingsFragment;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize fragments
        viewFoodFragment = new ViewFoodFragment();
        shoppingFragment = new ShoppingFragment();
        wasteFragment = new WasteFragment();
        settingsFragment = new SettingsFragment();

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        fabAdd = findViewById(R.id.btn_add);
        fabAdd.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddFoodFragment())
                    .addToBackStack(null)
                    .commit();
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof ViewFoodFragment) {
                fabAdd.show();
            } else {
                fabAdd.hide();
            }
        });

        // Load default fragment (Inventory)
        if (savedInstanceState == null) {
            loadFragment(viewFoodFragment);
            bottomNav.setSelectedItemId(R.id.nav_inventory);
        }
    }

    // Helper method to control FAB visibility from fragments if needed
    public void setFabVisibility(boolean visible) {
        if (visible) {
            fabAdd.show();
        } else {
            fabAdd.hide();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_inventory) {
                        selectedFragment = viewFoodFragment;
                        fabAdd.show();
                    } else if (itemId == R.id.nav_shopping) {
                        selectedFragment = shoppingFragment;
                        fabAdd.hide();
                    } else if (itemId == R.id.nav_waste) {
                        selectedFragment = wasteFragment;
                        fabAdd.hide();
                    } else if (itemId == R.id.nav_settings) {
                        selectedFragment = settingsFragment;
                        fabAdd.hide();
                    }

                    if (selectedFragment != null) {
                        loadFragment(selectedFragment);
                    }
                    return true;
                }
            };

    public void refreshFoodList() {
        if (viewFoodFragment != null && viewFoodFragment.isAdded()) {
            viewFoodFragment.refreshData();
        }
    }

    public void scheduleReminder(String foodName, String reminderDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date date = sdf.parse(reminderDate);
            Date today = new Date();

            Calendar calDate = Calendar.getInstance();
            calDate.setTime(date);
            calDate.set(Calendar.HOUR_OF_DAY, 12);
            calDate.set(Calendar.MINUTE, 0);
            calDate.set(Calendar.SECOND, 0);
            calDate.set(Calendar.MILLISECOND, 0);

            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            // If reminder is for today or past, show immediately
            if (!calDate.after(calToday)) {
                showImmediateNotification(foodName);
                return;
            }

            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("FOOD_NAME", foodName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    foodName.hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calDate.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calDate.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error scheduling reminder", e);
        }
    }

    private void showImmediateNotification(String foodName) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "food_reminder_channel",
                    "Food Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "food_reminder_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Food Reminder")
                .setContentText(foodName + " is expiring today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(foodName.hashCode(), builder.build());
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    public void switchToViewFood() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        try {
            if (viewFoodFragment == null) {
                viewFoodFragment = new ViewFoodFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, viewFoodFragment)
                    .commitNowAllowingStateLoss();

            new Handler(Looper.getMainLooper()).post(() -> {
                if (viewFoodFragment.isAdded()) {
                    viewFoodFragment.refreshData();
                }
            });
        } catch (IllegalStateException e) {
            Log.e("MainActivity", "Fragment transaction failed", e);
        }
    }
}