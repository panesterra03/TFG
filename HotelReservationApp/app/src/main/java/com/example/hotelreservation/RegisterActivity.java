package com.example.hotelreservation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    
    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private TextView txtErrorMessage;
    private Button btnRegister;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Inicializar vistas
        edtName = findViewById(R.id.edtRegisterName);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtRegisterConfirmPassword);
        txtErrorMessage = findViewById(R.id.txtRegisterErrorMessage);
        btnRegister = findViewById(R.id.btnRegister);
        
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }
    
    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        
        // Validaciones
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            txtErrorMessage.setText("Todos los campos son obligatorios");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            txtErrorMessage.setText("Las contraseñas no coinciden");
            return;
        }
        
        // Crear objeto para enviar al API
        Map<String, String> userData = new HashMap<>();
        userData.put("nombre", name);
        userData.put("correo", email);
        userData.put("contraseña", password);
        
        // Llamar al API
        ApiService apiService = ApiClient.getApiService();
        Call<JsonObject> call = apiService.registerUser(userData);
        
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso. Por favor inicia sesión.", Toast.LENGTH_LONG).show();
                        finish(); // Volver a la pantalla de login
                    } else {
                        String message = json.has("message") ? json.get("message").getAsString() : "Error en el registro";
                        txtErrorMessage.setText(message);
                    }
                } else {
                    txtErrorMessage.setText("Error en la respuesta del servidor");
                }
            }
            
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                txtErrorMessage.setText("Error de red: " + t.getMessage());
            }
        });
    }
}
