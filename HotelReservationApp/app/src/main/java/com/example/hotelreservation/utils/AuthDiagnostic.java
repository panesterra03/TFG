package com.example.hotelreservation.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.hotelreservation.MainActivity;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import android.util.Base64;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A utility class to diagnose and troubleshoot authentication issues
 */
public class AuthDiagnostic {
    private static final String TAG = "AuthDiagnostic";

    /**
     * Run diagnostic tests on authentication
     * @param context Application context
     * @param resultView TextView to display results (can be null)
     * @return Text with diagnostic results
     */
    public static String runDiagnostics(Context context, TextView resultView) {
        StringBuilder results = new StringBuilder();
        
        // Check if token exists
        SharedPreferences preferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        results.append("=== Diagnóstico de Autenticación ===\n\n");
        
        // Check 1: Token exists
        if (token == null || token.isEmpty()) {
            results.append("✗ No se encontró token de autenticación\n");
        } else {
            results.append("✓ Token encontrado\n");
            
            // Check 2: Token format
            if (TokenUtils.isTokenValid(token)) {
                results.append("✓ Formato de token válido\n");
                
                // Check 3: Parse token payload
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length == 3) {                        String payloadB64 = parts[1];
                        byte[] payloadBytes = Base64.decode(payloadB64, Base64.URL_SAFE);
                        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
                        
                        results.append("✓ Contenido del token decodificado:\n");
                        results.append(payload).append("\n");
                        
                        // Try to parse as JSON and check expiry
                        try {
                            JSONObject jsonPayload = new JSONObject(payload);
                            // Check if token has essential fields
                            if (jsonPayload.has("sub")) {
                                results.append("✓ Token contiene campo 'sub' (subject)\n");
                            } else {
                                results.append("✗ Token no contiene el campo 'sub' (subject)\n");
                            }
                            
                            // Check expiry if available
                            if (jsonPayload.has("exp")) {
                                long expiry = jsonPayload.getLong("exp");
                                long currentTime = System.currentTimeMillis() / 1000;
                                if (currentTime > expiry) {
                                    results.append("✗ Token expirado\n");
                                } else {
                                    results.append("✓ Token activo (no expirado)\n");
                                }
                            }
                        } catch (Exception e) {
                            results.append("✗ Error al procesar JSON del payload: ").append(e.getMessage()).append("\n");
                        }
                    }
                } catch (Exception e) {
                    results.append("✗ Error al decodificar token: ").append(e.getMessage()).append("\n");
                }
            } else {
                results.append("✗ Formato de token inválido\n");
            }
            
            // Check 4: Token with Bearer prefix
            results.append("✓ Token con prefijo Bearer: ").append(TokenUtils.ensureBearer(token)).append("\n");
        }
        
        // Check other authentication info
        String userRole = preferences.getString("USER_ROLE", "none");
        results.append("✓ Rol de usuario: ").append(userRole).append("\n");
        
        final String resultText = results.toString();
        Log.d(TAG, "Diagnóstico de autenticación:\n" + resultText);
        
        // Update UI if resultView is provided
        if (resultView != null) {
            resultView.setText(resultText);
            resultView.setVisibility(View.VISIBLE);
        }
        
        return resultText;
    }
    
    /**
     * Test a token against the API
     * @param context Application context
     * @param token Token to test
     * @param resultView TextView to display results
     */
    public static void testTokenWithApi(Context context, String token, TextView resultView) {
        if (token == null || token.isEmpty()) {
            updateTextView(resultView, "No hay token para probar");
            return;
        }
        
        String authHeader = TokenUtils.ensureBearer(token);
        
        // Try a simple API call that requires authentication
        ApiService apiService = ApiClient.getApiService();
        Call<List<String>> call = apiService.listarHoteles(authHeader);
        
        updateTextView(resultView, "Probando token con API...");
        
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                StringBuilder result = new StringBuilder();
                result.append("Respuesta API: ").append(response.code()).append("\n");
                if (response.isSuccessful() && response.body() != null) {
                    result.append("Hoteles recibidos: ").append(response.body().size()).append("\n");
                } else {
                    result.append("Error al obtener hoteles: ").append(response.message()).append("\n");
                }
                updateTextView(resultView, result.toString());
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                updateTextView(resultView, "Error de red: " + t.getMessage());
            }
        });
    }
    
    private static void updateTextView(TextView textView, String message) {
        if (textView != null && textView.getContext() != null) {
            textView.post(() -> textView.setText(message));
        }
        Log.d(TAG, message);
    }
    
    private static void promptRelogin(Context context, String reason) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("message", "Sesión inválida: " + reason);
        context.startActivity(intent);
    }
}
