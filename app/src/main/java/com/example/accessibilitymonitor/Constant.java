package com.example.accessibilitymonitor;

/**
 * A utility class to store constants for SharedPreferences keys and related app configurations.
 */
public final class Constant {

    // SharedPreferences file name
    private static final String PREF_NAME = "AppPrefs";

    // SharedPreferences keys for restricted apps
    private static final String WIFI_RESTRICTED_APPS_KEY = "wifi_restricted_apps";
    private static final String CELLULAR_RESTRICTED_APPS_KEY = "cellular_restricted_apps";

    // Private constructor to prevent instantiation
    private Constant() {
        throw new UnsupportedOperationException("Cannot instantiate a utility class.");
    }

    /**
     * Get the SharedPreferences file name.
     *
     * @return The name of the SharedPreferences file.
     */
    public static String getPrefName() {
        return PREF_NAME;
    }

    /**
     * Get the key for Wi-Fi restricted apps in SharedPreferences.
     *
     * @return The Wi-Fi restricted apps key.
     */
    public static String getWifiRestrictedAppsKey() {
        return WIFI_RESTRICTED_APPS_KEY;
    }

    /**
     * Get the key for Cellular restricted apps in SharedPreferences.
     *
     * @return The Cellular restricted apps key.
     */
    public static String getCellularRestrictedAppsKey() {
        return CELLULAR_RESTRICTED_APPS_KEY;
    }
}
