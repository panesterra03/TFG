package com.example.hotelreservation.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Utility class to handle token operations
 */
public class TokenUtils {
    private static final String TAG = "TokenUtils";

    /**
     * Verifies if a token is valid based on its format
     * @param token The token to verify
     * @return true if the token has a valid format, otherwise false
     */
    public static boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            Log.e(TAG, "Token is null or empty");
            return false;
        }
        
        // Check if token is in JWT format (has two dots)
        // JWT format: header.payload.signature
        if (!token.contains(".") || token.split("\\.").length != 3) {
            Log.e(TAG, "Token is not in valid JWT format (header.payload.signature): " + token);
            return false;
        }
        
        return true;
    }

    /**
     * Ensures token has the "Bearer " prefix required by the API
     * @param token Raw token string
     * @return Token with "Bearer " prefix
     */
    public static String ensureBearer(String token) {
        if (token == null || token.trim().isEmpty()) {
            return "";
        }
        
        if (token.startsWith("Bearer ")) {
            return token;
        }
        
        return "Bearer " + token;
    }

    /**
     * Retrieves the token from shared preferences and validates it
     * @param context Application context
     * @return Valid token with Bearer prefix or null if invalid
     */
    public static String getValidToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        if (!isTokenValid(token)) {
            Log.e(TAG, "Retrieved token is invalid: " + token);
            Toast.makeText(context, "Token inválido. Por favor, inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
            return null;
        }
        
        return ensureBearer(token);
    }
}
