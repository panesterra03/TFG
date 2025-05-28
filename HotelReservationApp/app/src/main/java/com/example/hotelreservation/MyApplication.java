package com.example.hotelreservation;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.utils.TokenUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize any app-wide configurations here
        Log.d(TAG, "Application initialized");
        
        // Check if API server is reachable
        checkServerConnectivity();
        
        // Verify stored tokens at startup
        verifyStoredTokens();
    }
    
    private void verifyStoredTokens() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        if (!token.isEmpty()) {
            Log.d(TAG, "Stored token found, validating...");
            if (TokenUtils.isTokenValid(token)) {
                Log.d(TAG, "Token format is valid");
            } else {
                Log.e(TAG, "Invalid token format detected at startup, clearing token");
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("token");
                editor.apply();
            }
        } else {
            Log.d(TAG, "No stored token found");
        }
    }
    
    private void checkServerConnectivity() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String serverUrl = "https://ebbc-62-174-205-48.ngrok-free.app/";
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("HEAD");
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Server connectivity check - Response Code: " + responseCode);
                
                if (responseCode >= 200 && responseCode < 400) {
                    Log.d(TAG, "Server is reachable");
                } else {
                    Log.e(TAG, "Server returned error code: " + responseCode);
                }
            } catch (IOException e) {
                Log.e(TAG, "Server connectivity check failed: " + e.getMessage(), e);
            }
        });
        executor.shutdown();
    }
}