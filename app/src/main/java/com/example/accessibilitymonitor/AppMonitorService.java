package com.example.accessibilitymonitor;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import java.util.HashSet;
import java.util.Set;

public class AppMonitorService extends AccessibilityService {
    private SharedPreferences preferences;
    private final Handler handler = new Handler();
    private final Runnable keepAliveRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("AppMonitorService", "Service keep-alive ping");

            handler.postDelayed(this, 10 * 60 * 1000); // Ping every 10 minutes
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d("AppMonitorService", "Service connected");
        // Start the keep-alive mechanism
        handler.post(keepAliveRunnable);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AppMonitorService ","THIS APPS MONITOR");
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Register BroadcastReceiver for real-time updates
        Intent filter = new Intent("com.example.ACCESSIBILITY_UPDATE");

        filter.putExtra("monitoring_enabled", true); // or false






//        registerReceiver(updateReceiver, filter);
        sendBroadcast(filter);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";

            boolean monitoringEnabled = preferences.getBoolean("monitoring_enabled", true);
            if (monitoringEnabled) {
                checkRestrictedApps(packageName);
            }
        }
    }

    public void showNotification(Context context, String packageName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "default_channel";
        Log.d("AppMonitorService", "Preparing to create notification channel");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
            Log.d("AppMonitorService", "Notification channel created.");
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Warning")
                .setContentText("Access to " + packageName + " should restricted with Wifi for your safety.")
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();


        notificationManager.notify(1, notification);
        Log.d("AppMonitorService", "Notification sent.");
    }



    private void checkRestrictedApps(String packageName) {
        Set<String> restrictedApps = new HashSet<>(preferences.getStringSet("restricted_apps", new HashSet<>()));

        Log.d("checkRestrictedApps", "Restricted Apps: " + restrictedApps);
        if (restrictedApps.contains(packageName)) {
            disableWiFi();
            Toast.makeText(this, "Prevented "+ packageName + " to use and wifi", Toast.LENGTH_SHORT).show();
            sendNotification(packageName);
            logEvent("Restricted app detected: " + packageName + " at " + System.currentTimeMillis());
        }
    }


    private void disableWiFi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false); // Disable Wi-Fi
            Log.d("AppMonitorService", "Wi-Fi disabled");
        } else {
            Log.d("AppMonitorService", "Wi-Fi is already disabled or WifiManager is null");
        }
    }
    @SuppressLint("NotificationPermission")
    private void sendNotification(String packageName) {
        String channelId = "RestrictedAppChannel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Restricted App Alerts", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Restricted App Detected")
                .setContentText("Wi-Fi has been disabled for " + packageName)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());
    }

    private void logEvent(String event) {
        SharedPreferences.Editor editor = preferences.edit();
        String logs = preferences.getString("logs", "");
        logs += event + "\n";
        editor.putString("logs", logs);
        editor.apply();
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            preferences = getSharedPreferences("AppMonitorPrefs", MODE_PRIVATE);
            if (intent.hasExtra("monitoring_enabled")) {
                boolean monitoringEnabled = intent.getBooleanExtra("monitoring_enabled", true);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("monitoring_enabled", monitoringEnabled);
                editor.apply();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        super.onDestroy();
        // Stop the keep-alive mechanism when the service is destroyed
        handler.removeCallbacks(keepAliveRunnable);
        unregisterReceiver(updateReceiver);
    }

    @Override
    public void onInterrupt() {
    }
}
