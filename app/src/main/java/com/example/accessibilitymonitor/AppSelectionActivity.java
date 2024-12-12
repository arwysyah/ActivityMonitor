package com.example.accessibilitymonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class AppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "AppSelectionActivity";

    private final List<AppInfo> appList = new ArrayList<>();
    private HashMap<String, String> selectedRestrictions = new HashMap<>();
    private HashMap<String, String> originalRestrictions = new HashMap<>(); // To track the original state
    private SharedPreferences prefs;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the up button (back arrow)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle back arrow click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // UI References
        RecyclerView appsRecyclerView = findViewById(R.id.apps_recycler_view);

        saveButton = findViewById(R.id.save_button); // Save button reference

        // Go back button action


        // Initialize SharedPreferences
        prefs = getSharedPreferences(Constant.getPrefName(), Context.MODE_PRIVATE);

        // Load previously selected restrictions
        loadSelectedRestrictions();

        // Load installed apps
        loadInstalledApps();

        // Setup RecyclerView
        AppListAdapter adapter = new AppListAdapter(this, appList, selectedRestrictions, this::onRestrictionsChanged);
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appsRecyclerView.setAdapter(adapter);

        // Initially disable save button and set color
        setSaveButtonState(false);

        // Save button action
        saveButton.setOnClickListener(v -> {
            saveSelectedRestrictions();
            setSaveButtonState(false); // Disable after saving
            Toast.makeText(this, "Restrictions saved successfully", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Callback for restriction changes in the adapter
     */
    private void onRestrictionsChanged() {
        if (!selectedRestrictions.equals(originalRestrictions)) {
            setSaveButtonState(true); // Enable button if there are changes
        } else {
            setSaveButtonState(false); // Disable button if there are no changes
        }
    }

    /**
     * Sets the save button state and color
     * @param enabled true to enable, false to disable
     */
    private void setSaveButtonState(boolean enabled) {
        saveButton.setEnabled(enabled);
        if (enabled) {
            saveButton.setBackgroundColor(getResources().getColor(R.color.button_enabled)); // Color when enabled
        } else {
            saveButton.setBackgroundColor(getResources().getColor(R.color.button_disabled)); // Color when disabled
        }
    }

    /**
     * Loads the user's previously saved restrictions from SharedPreferences.
     */
    private void loadSelectedRestrictions() {
        String json = prefs.getString(Constant.getWifiRestrictedAppsKey(), "{}");
        try {
            Type type = new TypeToken<HashMap<String, String>>() {}.getType();
            selectedRestrictions = new Gson().fromJson(json, type);

            if (selectedRestrictions == null) {
                selectedRestrictions = new HashMap<>();
            }

            // Save a copy of the initial restrictions
            originalRestrictions = new HashMap<>(selectedRestrictions);
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing selected restrictions: ", e);
            selectedRestrictions = new HashMap<>();
        }

        Log.d(TAG, "Selected restrictions loaded: " + selectedRestrictions);
    }

    /**
     * Saves the selected restrictions to SharedPreferences.
     */
    private void saveSelectedRestrictions() {
        if (selectedRestrictions == null) {
            selectedRestrictions = new HashMap<>();
        }

        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(selectedRestrictions);
        editor.putString(Constant.getWifiRestrictedAppsKey(), json);
        editor.apply();

        Log.d(TAG, "Selected restrictions saved: " + selectedRestrictions);

        // Update the original restrictions after saving
        originalRestrictions = new HashMap<>(selectedRestrictions);
    }

    /**
     * Loads the list of installed apps on the device.
     */
    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolvedApps = pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        for (ResolveInfo resolveInfo : resolvedApps) {
            String appName = resolveInfo.loadLabel(pm) != null
                    ? resolveInfo.loadLabel(pm).toString()
                    : "Unknown App";
            String packageName = resolveInfo.activityInfo.packageName;
            Drawable appIcon = resolveInfo.loadIcon(pm);

            appList.add(new AppInfo(appName, packageName, appIcon));
        }

        Log.d(TAG, "Installed apps loaded: " + appList.size());
    }
}
