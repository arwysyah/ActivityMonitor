package com.example.accessibilitymonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

public class SettingsActivity extends AppCompatActivity {
    private Switch monitorSwitch;
    private EditText appInput;
    private Button addAppButton, removeAppButton, showAppsButton, viewLogsButton;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start biometric authentication before showing settings
        checkBiometricAuthentication();
    }

    private void checkBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {

            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    initializeSettingsView();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(SettingsActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentication Required")
                    .setSubtitle("Access settings securely")
                    .setNegativeButtonText("Cancel")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "Biometric authentication not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeSettingsView() {
        setContentView(R.layout.activity_settings);

        monitorSwitch = findViewById(R.id.monitor_switch);
        appInput = findViewById(R.id.app_input);
        addAppButton = findViewById(R.id.add_app_button);
        removeAppButton = findViewById(R.id.remove_app_button);
        showAppsButton = findViewById(R.id.show_apps_button);
        viewLogsButton = findViewById(R.id.view_logs_button);

        preferences = getSharedPreferences("AppMonitorPrefs", MODE_PRIVATE);
        editor = preferences.edit();

        monitorSwitch.setChecked(preferences.getBoolean("monitoring_enabled", true));
        monitorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("monitoring_enabled", isChecked);
            editor.apply();
            Toast.makeText(this, "Monitoring " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            notifyService();
        });

        addAppButton.setOnClickListener(v -> {
            String app = appInput.getText().toString().trim();
            if (!app.isEmpty()) {
                Set<String> apps = preferences.getStringSet("restricted_apps", new HashSet<>());
                apps.add(app);
                editor.putStringSet("restricted_apps", apps);
                editor.apply();
                Toast.makeText(this, "App added: " + app, Toast.LENGTH_SHORT).show();
                notifyService();
            }
        });

        removeAppButton.setOnClickListener(v -> {
            String app = appInput.getText().toString().trim();
            if (!app.isEmpty()) {
                Set<String> apps = preferences.getStringSet("restricted_apps", new HashSet<>());
                if (apps.contains(app)) {
                    apps.remove(app);
                    editor.putStringSet("restricted_apps", apps);
                    editor.apply();
                    Toast.makeText(this, "App removed: " + app, Toast.LENGTH_SHORT).show();
                    notifyService();
                } else {
                    Toast.makeText(this, "App not found: " + app, Toast.LENGTH_SHORT).show();
                }
            }
        });

        showAppsButton.setOnClickListener(v -> {
            Set<String> apps = preferences.getStringSet("restricted_apps", new HashSet<>());
            Toast.makeText(this, "Restricted Apps: " + apps.toString(), Toast.LENGTH_LONG).show();
        });

        viewLogsButton.setOnClickListener(v -> {
            String logs = preferences.getString("logs", "No logs available.");
            Toast.makeText(this, logs, Toast.LENGTH_LONG).show();
        });
    }

    private void notifyService() {
        Intent intent = new Intent("com.example.ACCESSIBILITY_UPDATE");
        sendBroadcast(intent);
    }
}
