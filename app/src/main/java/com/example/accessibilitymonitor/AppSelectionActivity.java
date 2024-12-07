package com.example.accessibilitymonitor;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "AppSelectionActivity";
    private static final String PREF_NAME = "AppPrefs";
    private static final String RESTRICTED_APPS_KEY = "restricted_apps";

    private final List<AppInfo> appList = new ArrayList<>();
    private Set<String> selectedApps = new HashSet<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        ListView appsListView = findViewById(R.id.apps_list_view);
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load previously selected apps
        selectedApps = new HashSet<>(prefs.getStringSet(RESTRICTED_APPS_KEY, new HashSet<>()));

        // Load all installed apps
        loadInstalledApps();

        // Set adapter
        AppListAdapter adapter = new AppListAdapter(this, appList, selectedApps);
        appsListView.setAdapter(adapter);
        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        // Check for usage stats permission
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save selected apps to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(RESTRICTED_APPS_KEY, selectedApps);
        editor.apply();
        Log.d(TAG, "Selected apps saved: " + selectedApps);
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolvedApps = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo resolveInfo : resolvedApps) {
            String appName = resolveInfo.loadLabel(pm).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            Drawable appIcon = resolveInfo.loadIcon(pm);

            appList.add(new AppInfo(appName, packageName, appIcon));
        }
        Log.d(TAG, "Installed apps loaded: " + appList.size());
    }

    private boolean hasUsageStatsPermission() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        return stats != null && !stats.isEmpty();
    }

    private void requestUsageStatsPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required")
                .setMessage("This app requires usage access to monitor app usage. Please enable it in the settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Usage stats permission is required to monitor apps.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private static class AppInfo {
        String name;
        String packageName;
        Drawable icon;

        AppInfo(String name, String packageName, Drawable icon) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
        }
    }

    private static class AppListAdapter extends BaseAdapter {
        private final Context context;
        private final List<AppInfo> appList;
        private final Set<String> selectedApps;

        AppListAdapter(Context context, List<AppInfo> appList, Set<String> selectedApps) {
            this.context = context;
            this.appList = appList;
            this.selectedApps = selectedApps;
        }

        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public Object getItem(int position) {
            return appList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_app_info, parent, false);
            }

            AppInfo appInfo = appList.get(position);

            ImageView appIcon = convertView.findViewById(R.id.app_icon);
            TextView appName = convertView.findViewById(R.id.app_name);
            TextView appPackage = convertView.findViewById(R.id.app_package);
            CheckBox appCheckBox = convertView.findViewById(R.id.app_checkbox);

            appIcon.setImageDrawable(appInfo.icon);
            appName.setText(appInfo.name);
            appPackage.setText(appInfo.packageName);
            appCheckBox.setChecked(selectedApps.contains(appInfo.packageName));

            appCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedApps.add(appInfo.packageName);
                } else {
                    selectedApps.remove(appInfo.packageName);
                }
                Log.d("AppListAdapter", "App selection changed: " + appInfo.packageName + " -> " + isChecked);
            });

            return convertView;
        }
    }
}
