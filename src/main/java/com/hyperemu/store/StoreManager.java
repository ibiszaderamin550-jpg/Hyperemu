package com.hyperemu.store;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * HyperEmu Store Manager
 * Manages Steam, GOG, and Amazon Games integration.
 * 
 * HOW IT WORKS:
 * - Stores user credentials/tokens securely in SharedPreferences
 * - Provides pre-configured container shortcuts for each store
 * - Each store client (Steam, GOG Galaxy, Amazon Games) runs inside
 *   a Wine container just like any Windows app
 * - The manager sets up the correct Wine environment variables and
 *   registry entries for each store's launcher to work properly
 */
public class StoreManager {
    public enum StoreType {
        STEAM,
        GOG,
        AMAZON
    }

    // Steam download URL for the installer
    public static final String STEAM_INSTALLER_URL = "https://cdn.cloudflare.steamstatic.com/client/installer/SteamSetup.exe";
    // GOG Galaxy installer
    public static final String GOG_INSTALLER_URL = "https://webinstallers.gog-statics.com/download/GOG_Galaxy_2.0.exe";
    // Amazon Games installer  
    public static final String AMAZON_INSTALLER_URL = "https://download.amazongames.com/AmazonGamesSetup.exe";

    // Wine registry paths where stores install themselves
    public static final String STEAM_WINE_PATH = "C:\\Program Files (x86)\\Steam\\Steam.exe";
    public static final String GOG_WINE_PATH = "C:\\Program Files (x86)\\GOG Galaxy\\GalaxyClient.exe";
    public static final String AMAZON_WINE_PATH = "C:\\Users\\Public\\Amazon Games\\App\\Amazon Games.exe";

    // Prefs keys
    private static final String PREF_STEAM_TOKEN = "hyperemu_steam_token";
    private static final String PREF_STEAM_USERNAME = "hyperemu_steam_username";
    private static final String PREF_GOG_TOKEN = "hyperemu_gog_token";
    private static final String PREF_GOG_USERNAME = "hyperemu_gog_username";
    private static final String PREF_AMAZON_TOKEN = "hyperemu_amazon_token";
    private static final String PREF_AMAZON_USERNAME = "hyperemu_amazon_username";
    private static final String PREF_STEAM_INSTALLED = "hyperemu_steam_installed";
    private static final String PREF_GOG_INSTALLED = "hyperemu_gog_installed";
    private static final String PREF_AMAZON_INSTALLED = "hyperemu_amazon_installed";

    private final SharedPreferences prefs;
    private final Context context;

    public StoreManager(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Save store session token after successful login
     */
    public void saveStoreToken(StoreType store, String username, String token) {
        SharedPreferences.Editor editor = prefs.edit();
        switch (store) {
            case STEAM:
                editor.putString(PREF_STEAM_TOKEN, token);
                editor.putString(PREF_STEAM_USERNAME, username);
                break;
            case GOG:
                editor.putString(PREF_GOG_TOKEN, token);
                editor.putString(PREF_GOG_USERNAME, username);
                break;
            case AMAZON:
                editor.putString(PREF_AMAZON_TOKEN, token);
                editor.putString(PREF_AMAZON_USERNAME, username);
                break;
        }
        editor.apply();
    }

    /**
     * Get saved username for a store
     */
    public String getUsername(StoreType store) {
        switch (store) {
            case STEAM: return prefs.getString(PREF_STEAM_USERNAME, "");
            case GOG: return prefs.getString(PREF_GOG_USERNAME, "");
            case AMAZON: return prefs.getString(PREF_AMAZON_USERNAME, "");
            default: return "";
        }
    }

    /**
     * Check if user has a saved session for a store
     */
    public boolean isLoggedIn(StoreType store) {
        switch (store) {
            case STEAM: return !prefs.getString(PREF_STEAM_TOKEN, "").isEmpty();
            case GOG: return !prefs.getString(PREF_GOG_TOKEN, "").isEmpty();
            case AMAZON: return !prefs.getString(PREF_AMAZON_TOKEN, "").isEmpty();
            default: return false;
        }
    }

    /**
     * Clear saved session (logout)
     */
    public void logout(StoreType store) {
        SharedPreferences.Editor editor = prefs.edit();
        switch (store) {
            case STEAM:
                editor.remove(PREF_STEAM_TOKEN);
                editor.remove(PREF_STEAM_USERNAME);
                break;
            case GOG:
                editor.remove(PREF_GOG_TOKEN);
                editor.remove(PREF_GOG_USERNAME);
                break;
            case AMAZON:
                editor.remove(PREF_AMAZON_TOKEN);
                editor.remove(PREF_AMAZON_USERNAME);
                break;
        }
        editor.apply();
    }

    /**
     * Mark a store as installed in a container
     */
    public void setInstalled(StoreType store, boolean installed) {
        SharedPreferences.Editor editor = prefs.edit();
        switch (store) {
            case STEAM: editor.putBoolean(PREF_STEAM_INSTALLED, installed); break;
            case GOG: editor.putBoolean(PREF_GOG_INSTALLED, installed); break;
            case AMAZON: editor.putBoolean(PREF_AMAZON_INSTALLED, installed); break;
        }
        editor.apply();
    }

    /**
     * Check if store client is installed in any container
     */
    public boolean isInstalled(StoreType store) {
        switch (store) {
            case STEAM: return prefs.getBoolean(PREF_STEAM_INSTALLED, false);
            case GOG: return prefs.getBoolean(PREF_GOG_INSTALLED, false);
            case AMAZON: return prefs.getBoolean(PREF_AMAZON_INSTALLED, false);
            default: return false;
        }
    }

    /**
     * Get installer download URL for a store
     */
    public static String getInstallerUrl(StoreType store) {
        switch (store) {
            case STEAM: return STEAM_INSTALLER_URL;
            case GOG: return GOG_INSTALLER_URL;
            case AMAZON: return AMAZON_INSTALLER_URL;
            default: return "";
        }
    }

    /**
     * Get the Wine executable path for a store after installation
     */
    public static String getWinePath(StoreType store) {
        switch (store) {
            case STEAM: return STEAM_WINE_PATH;
            case GOG: return GOG_WINE_PATH;
            case AMAZON: return AMAZON_WINE_PATH;
            default: return "";
        }
    }

    /**
     * Get recommended Wine environment variables for each store
     * to maximize compatibility
     */
    public static String getRecommendedEnvVars(StoreType store) {
        StringBuilder vars = new StringBuilder();
        // Common performance vars
        vars.append("DXVK_ASYNC=1\n");
        vars.append("DXVK_STATE_CACHE=1\n");
        vars.append("WINE_LARGE_ADDRESS_AWARE=1\n");
        vars.append("__GL_THREADED_OPTIMIZATIONS=1\n");

        switch (store) {
            case STEAM:
                vars.append("STEAM_COMPAT_DATA_PATH=/data/data/com.hyperemu/steam_compat\n");
                vars.append("SteamAppId=0\n");
                break;
            case GOG:
                vars.append("GALAXY_CLIENT=1\n");
                break;
            case AMAZON:
                vars.append("AMAZON_GAMES=1\n");
                break;
        }
        return vars.toString();
    }

    /**
     * Get store display name
     */
    public static String getStoreName(StoreType store) {
        switch (store) {
            case STEAM: return "Steam";
            case GOG: return "GOG Galaxy";
            case AMAZON: return "Amazon Games";
            default: return "Unknown";
        }
    }
}
