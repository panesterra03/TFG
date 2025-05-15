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
        // Limpia los datos de sesión al iniciar la aplicación
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Elimina todos los datos almacenados
        editor.apply();
    }

    public void btnLoginClick(View view) {
        String username = edtLoginUsername.getText().toString().trim();
        String password = edtLoginPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            txtErrorMessage.setText("Por favor ingresa usuario y contraseña.");
            return;
        }

        // Crear JSON simple para enviar
        Map<String, String> loginData = new HashMap<>();
        loginData.put("correo", username);
        loginData.put("contraseña", password);

        ApiService apiService = ApiClient.getApiService();
        Call<JsonObject> call = apiService.login(loginData);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();

                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        // Login correcto
                        String rol = json.get("rol").getAsString();
                        int userId = json.get("userId").getAsInt();
                        String Token = json.get("token").getAsString();//con posibilidad de cambio
                        // Guardar si quieres
                        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", Token);//con posibilidad de cambio
                        editor.putString("USER_ROLE", rol);
                        editor.putInt("USER_ID", userId);
                        editor.apply();

                        // Ir al menú principal
                        navigateToMainMenu(rol);

                    } else {
                        // Mostrar mensaje de error que viene del backend
                        String message = json.has("message") ? json.get("message").getAsString() : "Error desconocido";
                        txtErrorMessage.setText(message);
                    }
                } else {
                    txtErrorMessage.setText("Error en la respuesta del servidor.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("LoginError", t.getMessage());
                txtErrorMessage.setText("Error de red: " + t.getMessage());
            }
        });
    }


    private void navigateToMainMenu(String userRole) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
        finish(); // Close the login activity
    }


}
