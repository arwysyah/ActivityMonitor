package com.example.accessibilitymonitor;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button requestNotificationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        Button openAccessibilityButton = findViewById(R.id.open_accessibility_button);
        requestNotificationButton = findViewById(R.id.request_notification_button);
       Button selectionAppsButton = findViewById(R.id.selection_apps_button);
        Button openSettingsButton = findViewById(R.id.open_settings_button); // New button

        // Check and display the status of the Accessibility Service
        updateAccessibilityStatus();

        openAccessibilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Enable the Accessibility Service for this app", Toast.LENGTH_LONG).show();
        });

        // Request notification permissions if required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        } else {
            requestNotificationButton.setEnabled(false);
        }
        selectionAppsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,AppSelectionActivity.class);
            startActivity(intent);
        });
        openSettingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccessibilityStatus();
    }

    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
                    requestNotificationButton.setEnabled(false);
                    requestNotificationButton.setText("Notification permission granted");
                } else {
                    Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
                }
            });

    private void checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestNotificationButton.setOnClickListener(v ->
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS));
        } else {
            requestNotificationButton.setEnabled(false);
            requestNotificationButton.setText("Notification permission granted");
        }
    }

    private void updateAccessibilityStatus() {
        boolean isEnabled = isAccessibilityServiceEnabled(this, AppMonitorService.class);
        statusText.setText(isEnabled ? "Accessibility Service is enabled." : "Accessibility Service is disabled.");
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        ComponentName expectedComponentName = new ComponentName(context, serviceClass);
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices == null) {
            return false;
        }
        for (String enabledService : enabledServices.split(":")) {
            ComponentName componentName = ComponentName.unflattenFromString(enabledService);
            if (componentName != null && componentName.equals(expectedComponentName)) {
                return true;
            }
        }
        return false;
    }
}
