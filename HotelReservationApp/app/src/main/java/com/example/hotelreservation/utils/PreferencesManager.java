package com.example.hotelreservation.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Utility class for handling shared preferences defaults and backups
 */
public class PreferencesManager {
    private static final String TAG = "PreferencesManager";
    private static final String MAIN_PREFS = "UserPrefs";
    private static final String BACKUP_PREFS = "UserPrefsBackup";

    /**
     * Backs up important preferences to a secondary storage
     * @param context Application context
     */
    public static void backupPreferences(Context context) {
        SharedPreferences mainPrefs = context.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE);
        SharedPreferences backupPrefs = context.getSharedPreferences(BACKUP_PREFS, Context.MODE_PRIVATE);
        
        SharedPreferences.Editor backupEditor = backupPrefs.edit();
        
        // Back up token and user information
        String token = mainPrefs.getString("token", "");
        String role = mainPrefs.getString("USER_ROLE", "");
        int userId = mainPrefs.getInt("USER_ID", -1);
        
        backupEditor.putString("token", token);
        backupEditor.putString("USER_ROLE", role);
        backupEditor.putInt("USER_ID", userId);
        backupEditor.putLong("backup_time", System.currentTimeMillis());
        
        boolean success = backupEditor.commit(); // Using commit() for synchronous operation
        
        if (success) {
            Log.d(TAG, "User preferences backed up successfully");
        } else {
            Log.e(TAG, "Failed to back up user preferences");
        }
    }
    
    /**
     * Restores preferences from backup if main preferences are corrupted or empty
     * @param context Application context
     * @return true if restore was performed, false otherwise
     */
    public static boolean restoreFromBackupIfNeeded(Context context) {
        SharedPreferences mainPrefs = context.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE);
        
        // Check if main preferences are missing essential data
        String token = mainPrefs.getString("token", "");
        String role = mainPrefs.getString("USER_ROLE", "");
        
        // If we have a token but missing role, or vice versa, might indicate corruption
        if ((token.isEmpty() && !role.isEmpty()) || (!token.isEmpty() && role.isEmpty())) {
            return restoreFromBackup(context);
        }
        
        return false;
    }
    
    /**
     * Force restores preferences from backup
     * @param context Application context
     * @return true if restore was successful, false otherwise
     */
    public static boolean restoreFromBackup(Context context) {
        SharedPreferences mainPrefs = context.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE);
        SharedPreferences backupPrefs = context.getSharedPreferences(BACKUP_PREFS, Context.MODE_PRIVATE);
        
        // Check if backup exists
        if (!backupPrefs.contains("backup_time")) {
            Log.d(TAG, "No backup available to restore");
            return false;
        }
        
        // Restore from backup
        SharedPreferences.Editor mainEditor = mainPrefs.edit();
        
        String token = backupPrefs.getString("token", "");
        String role = backupPrefs.getString("USER_ROLE", "");
        int userId = backupPrefs.getInt("USER_ID", -1);
        
        mainEditor.putString("token", token);
        mainEditor.putString("USER_ROLE", role);
        mainEditor.putInt("USER_ID", userId);
        
        boolean success = mainEditor.commit(); // Using commit() for synchronous operation
        
        if (success) {
            Log.d(TAG, "User preferences restored from backup");
            return true;
        } else {
            Log.e(TAG, "Failed to restore user preferences from backup");
            return false;
        }
    }
    
    /**
     * Clears all user preferences (logout)
     * @param context Application context
     */
    public static void clearAllPreferences(Context context) {
        SharedPreferences mainPrefs = context.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE);
        SharedPreferences backupPrefs = context.getSharedPreferences(BACKUP_PREFS, Context.MODE_PRIVATE);
        
        mainPrefs.edit().clear().apply();
        backupPrefs.edit().clear().apply();
        
        Log.d(TAG, "All user preferences cleared");
    }
}
