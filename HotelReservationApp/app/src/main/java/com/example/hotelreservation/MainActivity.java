package com.example.hotelreservation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelreservation.utils.TokenUtils;
import com.example.hotelreservation.utils.AuthDiagnostic;
import com.example.hotelreservation.utils.PreferencesManager;

import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import com.example.hotelreservation.api.LoginRequest;
import com.example.hotelreservation.api.LoginResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Variables para login
    private EditText edtLoginUsername, edtLoginPassword;
    private TextView txtErrorMessage;

    // Variables para registro
    private EditText edtRegisterEmail, edtRegisterPassword, edtRegisterConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar campos de login
        edtLoginUsername = findViewById(R.id.edtUsername);
        edtLoginPassword = findViewById(R.id.edtPassword);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);


        // edtRegisterEmail = findViewById(R.id.emailEditText); // Inicializar campos de registro
        // edtRegisterPassword = findViewById(R.id.passwordEditText);
        // edtRegisterConfirmPassword = findViewById(R.id.confirmPasswordEditText);

        checkExistingSession();
        // buttonRegistroUsuario();
    }

    private void checkExistingSession() {
        // Try to restore from backup if needed
        boolean restored = PreferencesManager.restoreFromBackupIfNeeded(this);
        
        if (restored) {
            Log.d(TAG, "Restored preferences from backup");
            // Check if we have a valid token in the restored data
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String token = preferences.getString("token", "");
            if (!token.isEmpty() && TokenUtils.isTokenValid(token)) {
                Log.d(TAG, "Valid token found in restored preferences");
                // Don't clear session in this case
                return;
            }
        }
        
        // Limpia los datos de sesión al iniciar la aplicación
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Elimina todos los datos almacenados
        editor.apply();
        
        Log.d(TAG, "Session cleaned on startup");
    }

    public void btnLoginClick(View view) {
        String username = edtLoginUsername.getText().toString().trim();
        String password = edtLoginPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            txtErrorMessage.setText("Por favor ingresa usuario y contraseña.");
            return;
        }

        txtErrorMessage.setText("Conectando con el servidor...");
        
        // Log the connection attempt
        Log.d(TAG, "Iniciando login para usuario: " + username);

        // Crear JSON simple para enviar
        Map<String, String> loginData = new HashMap<>();
        loginData.put("correo", username);
        loginData.put("contraseña", password);

        ApiService apiService = ApiClient.getApiService();
        Call<JsonObject> call = apiService.login(loginData);

        Log.d(TAG, "Enviando solicitud de login a: " + call.request().url());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "Respuesta recibida con código: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    Log.d(TAG, "Cuerpo de respuesta: " + json.toString());

                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        // Login correcto
                        String rol = json.get("rol").getAsString();
                        int userId = json.get("userId").getAsInt();
                        
                        // Verificar que el token existe y procesarlo
                        if (!json.has("token") || json.get("token").isJsonNull()) {
                            txtErrorMessage.setText("Error: El servidor no proporcionó un token válido");
                            Log.e(TAG, "Error: Token ausente o nulo en la respuesta");
                            return;
                        }
                        
                        String token = json.get("token").getAsString();
                        
                        // Si el token está vacío, mostrar error
                        if (token == null || token.trim().isEmpty()) {
                            txtErrorMessage.setText("Error: Token vacío o inválido");
                            Log.e(TAG, "Error: Token vacío o inválido");
                            return;
                        }
                        
                        Log.d(TAG, "Token obtenido del servidor: " + token);
                        
                        // Guardar datos en SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", token);
                        editor.putString("USER_ROLE", rol);
                        editor.putInt("USER_ID", userId);
                        
                        // Para depuración, recuperar inmediatamente el token para verificar
                        editor.apply();
                        String tokenVerification = preferences.getString("token", "NO_TOKEN_FOUND");
                        Log.d(TAG, "Token verificación guardado: " + tokenVerification);
                        
                        if (!token.equals(tokenVerification)) {
                            Log.e(TAG, "¡Advertencia! El token guardado no coincide con el recibido");
                        }
                        
                        // Verificar formato del token
                        if (!TokenUtils.isTokenValid(token)) {
                            Log.e(TAG, "El token recibido no tiene un formato válido");
                            txtErrorMessage.setText("Error: El token recibido no tiene un formato JWT válido.");
                            return;
                        } else {
                            Log.d(TAG, "Token con formato válido");
                        }
                        
                        // Ejecutar diagnóstico de autenticación
                        AuthDiagnostic.runDiagnostics(MainActivity.this, null);
                        
                        // Guardar hotelId y hotelName si es hotelmanager
                        if ("hotelmanager".equals(rol) && json.has("hotelId")) {
                            int hotelId = json.get("hotelId").getAsInt();
                            String hotelName = json.has("hotelName") ? json.get("hotelName").getAsString() : "";
                            editor.putInt("HOTEL_ID", hotelId);
                            editor.putString("HOTEL_NAME", hotelName);
                            Log.d(TAG, "Datos de hotel guardados - ID: " + hotelId + ", Nombre: " + hotelName);
                        }
                        
                        editor.apply();
                        
                        // Crear backup de las preferencias
                        PreferencesManager.backupPreferences(MainActivity.this);
                        
                        Toast.makeText(MainActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                        // Ir al menú principal
                        navigateToMainMenu(rol);

                    } else {
                        // Mostrar mensaje de error que viene del backend
                        String message = json.has("message") ? json.get("message").getAsString() : "Error desconocido";
                        txtErrorMessage.setText(message);
                        Log.e(TAG, "Error de login desde servidor: " + message);
                    }
                } else {
                    try {
                        // Intentar obtener más información sobre el error
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            txtErrorMessage.setText("Error en la respuesta del servidor: " + response.code() + 
                                                  "\nDetalles: " + errorBody);
                        } else {
                            txtErrorMessage.setText("Error en la respuesta del servidor: " + response.code() + 
                                                  "\nRevise la conexión e intente nuevamente.");
                        }
                        Log.e(TAG, "Error en la respuesta: " + response.code() + " - " + response.message());
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar respuesta de error", e);
                        txtErrorMessage.setText("Error en la respuesta del servidor: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error de red en login", t);
                txtErrorMessage.setText("Error de conexión: " + t.getMessage() + 
                                      "\nVerifique su conexión a Internet y la disponibilidad del servidor.");
            }
        });
    }


    private void navigateToMainMenu(String userRole) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
        finish(); // Close the login activity
    }
    
    public void btnRegisterClick(View view) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
    
    public void btnDiagnosticClick(View view) {
        Intent intent = new Intent(MainActivity.this, com.example.hotelreservation.utils.AuthDiagnosticActivity.class);
        startActivity(intent);
    }
}
