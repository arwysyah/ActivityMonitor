package com.example.accessibilitymonitor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

//public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
//    private final Context context;
//    private final List<AppInfo> appList;
//    private final HashMap<String, String> selectedRestrictions;
//
//    public AppListAdapter(Context context, List<AppInfo> appList, HashMap<String, String> selectedRestrictions) {
//        this.context = context;
//        this.appList = appList;
//        this.selectedRestrictions = selectedRestrictions;
//    }
//
//    @NonNull
//    @Override
//    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_app_info, parent, false);
//        return new AppViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
//        AppInfo app = appList.get(position);
//
//        // Set app name, package, and icon
//        holder.appName.setText(app.getName());
//        holder.appPackage.setText(app.getPackageName());
//        holder.appIcon.setImageDrawable(app.getIcon());
//
//        // Set spinner selection
//        holder.spinner.setSelection(getRestrictionIndex(app.getPackageName()));
//
//        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String restriction = holder.spinner.getItemAtPosition(position).toString();
//                if (!restriction.equals("None")) {
//                    selectedRestrictions.put(app.getPackageName(), restriction);
//                } else {
//                    selectedRestrictions.remove(app.getPackageName());
//                }
//                Log.d("AppListAdapter", "Restriction updated: " + app.getPackageName() + " -> " + restriction);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Handle case when no item is selected, if necessary
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return appList.size();
//    }
//
//    private int getRestrictionIndex(String packageName) {
//        String restriction = selectedRestrictions.get(packageName);
//        if (restriction == null) return 0;
//        switch (restriction) {
//            case "Wi-Fi":
//                return 1;
//            case "Cellular":
//                return 2;
//            case "Wi-Fi and Cellular":
//                return 3;
//            default:
//                return 0;
//        }
//    }
//
//    static class AppViewHolder extends RecyclerView.ViewHolder {
//        TextView appName, appPackage;
//        ImageView appIcon; // Added for the app icon
//        Spinner spinner;
//
//        public AppViewHolder(@NonNull View itemView) {
//            super(itemView);
//            appName = itemView.findViewById(R.id.app_name);
//            appPackage = itemView.findViewById(R.id.app_package);
//            appIcon = itemView.findViewById(R.id.app_icon); // Initialize the ImageView
//            spinner = itemView.findViewById(R.id.restriction_spinner);
//        }
//    }
//}


public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private final Context context;
    private final List<AppInfo> appList;
    private final HashMap<String, String> selectedRestrictions;
    private final OnRestrictionChangeListener changeListener;

    // Callback interface to notify activity of changes
    public interface OnRestrictionChangeListener {
        void onRestrictionsChanged();
    }

    public AppListAdapter(Context context, List<AppInfo> appList, HashMap<String, String> selectedRestrictions, OnRestrictionChangeListener changeListener) {
        this.context = context;
        this.appList = appList;
        this.selectedRestrictions = selectedRestrictions;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_info, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);

        // Set app name, package, and icon
        holder.appName.setText(app.getName());
        holder.appPackage.setText(app.getPackageName());
        holder.appIcon.setImageDrawable(app.getIcon());

        // Set spinner selection
        holder.spinner.setSelection(getRestrictionIndex(app.getPackageName()));

        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String restriction = holder.spinner.getItemAtPosition(position).toString();
                if (!restriction.equals("None")) {
                    selectedRestrictions.put(app.getPackageName(), restriction);
                } else {
                    selectedRestrictions.remove(app.getPackageName());
                }

                Log.d("AppListAdapter", "Restriction updated: " + app.getPackageName() + " -> " + restriction);

                // Notify activity that restrictions have changed
                changeListener.onRestrictionsChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when no item is selected, if necessary
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    private int getRestrictionIndex(String packageName) {
        String restriction = selectedRestrictions.get(packageName);
        if (restriction == null) return 0;
        switch (restriction) {
            case "Wi-Fi":
                return 1;
            case "Cellular":
                return 2;
            case "Wi-Fi and Cellular":
                return 3;
            default:
                return 0;
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView appName, appPackage;
        ImageView appIcon; // Added for the app icon
        Spinner spinner;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            appPackage = itemView.findViewById(R.id.app_package);
            appIcon = itemView.findViewById(R.id.app_icon); // Initialize the ImageView
            spinner = itemView.findViewById(R.id.restriction_spinner);
        }
    }
}
