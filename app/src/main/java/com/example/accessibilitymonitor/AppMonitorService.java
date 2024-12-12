package com.example.accessibilitymonitor;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class AppMonitorService extends AccessibilityService {
    private static final String TAG = "AppMonitorService";

    private SharedPreferences prefs;
    private HashMap<String, String> selectedRestrictions = new HashMap<>();
    private final Handler handler = new Handler();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service connected");

        // Initialize SharedPreferences
        prefs = getSharedPreferences(Constant.getPrefName(), Context.MODE_PRIVATE);

        // Load initial restrictions
        loadSelectedRestrictions();

        // Listen for changes in restrictions
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (Constant.getWifiRestrictedAppsKey().equals(key)) {
                loadSelectedRestrictions();
                Log.d(TAG, "Restrictions updated dynamically: " + selectedRestrictions);
            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Reload restrictions before processing events
        loadSelectedRestrictions();

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            Log.d(TAG, "Current apps: " + packageName + " SELECTED " + selectedRestrictions);

            if (selectedRestrictions.containsKey(packageName)) {
                handleRestrictedApp(packageName);
            }
        }
    }

    private void handleRestrictedApp(String packageName) {
        String restrictionType = selectedRestrictions.get(packageName);
        Log.d(TAG, "Handling restriction for " + packageName + ": " + restrictionType);

        if ("Wi-Fi".equalsIgnoreCase(restrictionType)) {
            // Disable Wi-Fi functionality
            disableWiFi();

            // Show a toast message to notify user
            handler.post(() -> {
                Toast.makeText(this, "Wi-Fi shoule be disable for " + packageName, Toast.LENGTH_SHORT).show();
            });

            // Send a notification for Wi-Fi restriction
            sendNotification(packageName);

        } else if ("Cellular".equalsIgnoreCase(restrictionType)) {
            // Handle the Cellular restriction (you may need to add logic here)
            Log.d(TAG, "Cellular restriction triggered for " + packageName);
            handler.post(() -> {
                Toast.makeText(this, "Cellular should be disable for " + packageName, Toast.LENGTH_SHORT).show();
            });
            // Additional actions for cellular restriction, if needed

        } else if ("Wi-Fi and Cellular".equalsIgnoreCase(restrictionType)) {
            // Handle the case where both Wi-Fi and Cellular restrictions are applied
            disableWiFi();


            // Show a toast message to notify user
            handler.post(() -> {
                Toast.makeText(this, "Wi-Fi and Cellular disabled for " + packageName, Toast.LENGTH_SHORT).show();
            });

            // Send notification for both restrictions
            sendNotification(packageName);
        } else {
            // Handle the case where no restriction is applied or the restriction type is invalid
            Log.d(TAG, "No restriction or invalid restriction type for " + packageName);
        }

    }

    private void disableWiFi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            Log.d(TAG, "Wi-Fi disabled");
        } else {
            Log.d(TAG, "Wi-Fi is already disabled or WifiManager is null");
        }
    }

    private void sendNotification(String packageName) {
        String channelId = "RestrictedAppChannel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Restricted App Alerts", NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Restricted App Detected")
                .setContentText("Wi-Fi has been disabled for " + packageName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(1, notification);
    }

    private void loadSelectedRestrictions() {
        String json = prefs.getString(Constant.getWifiRestrictedAppsKey(), "{}");
        try {
            Type type = new TypeToken<HashMap<String, String>>() {}.getType();
            selectedRestrictions = new Gson().fromJson(json, type);

            if (selectedRestrictions == null) {
                selectedRestrictions = new HashMap<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing selected restrictions: ", e);
            selectedRestrictions = new HashMap<>();
        }
        Log.d(TAG, "Loaded restrictions: " + selectedRestrictions);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener((sharedPreferences, key) -> {});
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }
}
