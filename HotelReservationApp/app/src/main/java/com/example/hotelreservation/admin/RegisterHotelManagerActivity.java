package com.example.hotelreservation.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelreservation.R;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterHotelManagerActivity extends AppCompatActivity {
    
    private EditText edtName, edtEmail, edtPassword;
    private TextView txtErrorMessage;
    private Button btnRegister;
    private Spinner spinnerHotels;
    private List<String> hotelNames = new ArrayList<>();
    private List<Long> hotelIds = new ArrayList<>();
    private Long selectedHotelId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_hotel_manager);
        
        // Inicializar vistas
        edtName = findViewById(R.id.edtHotelManagerName);
        edtEmail = findViewById(R.id.edtHotelManagerEmail);
        edtPassword = findViewById(R.id.edtHotelManagerPassword);
        txtErrorMessage = findViewById(R.id.txtHotelManagerErrorMessage);
        btnRegister = findViewById(R.id.btnRegisterHotelManager);
        spinnerHotels = findViewById(R.id.spinnerHotels);
        
        // Cargar lista de hoteles
        loadHotels();
        
        // Configurar spinner
        spinnerHotels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < hotelIds.size()) {
                    selectedHotelId = hotelIds.get(position);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHotelId = null;
            }
        });
        
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerHotelManager();
            }
        });
    }
    
    private void loadHotels() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        if (token.isEmpty()) {
            txtErrorMessage.setText("No se encontró token de autenticación");
            return;
        }
        
        ApiService apiService = ApiClient.getApiService();
        Call<List<String>> call = apiService.listarHoteles("Bearer " + token);
        
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hotelNames.clear();
                    hotelIds.clear();
                    
                    List<String> hotels = response.body();
                    for (int i = 0; i < hotels.size(); i++) {
                        String hotelName = hotels.get(i);
                        hotelNames.add(hotelName);
                        hotelIds.add((long) (i + 1)); // Esto es una solución temporal, idealmente el API devolvería el ID
                    }
                    
                    // Configurar adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            RegisterHotelManagerActivity.this, 
                            android.R.layout.simple_spinner_item, 
                            hotelNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerHotels.setAdapter(adapter);
                    
                    if (!hotelNames.isEmpty()) {
                        selectedHotelId = hotelIds.get(0);
                    }
                } else {
                    txtErrorMessage.setText("Error al cargar los hoteles");
                }
            }
            
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                txtErrorMessage.setText("Error de red: " + t.getMessage());
            }
        });
    }
    
    private void registerHotelManager() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        
        // Validaciones
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            txtErrorMessage.setText("Todos los campos son obligatorios");
            return;
        }
        
        if (selectedHotelId == null) {
            txtErrorMessage.setText("Debe seleccionar un hotel");
            return;
        }
        
        // Crear objeto para enviar al API
        Map<String, String> userData = new HashMap<>();
        userData.put("nombre", name);
        userData.put("correo", email);
        userData.put("contraseña", password);
        userData.put("hotelId", String.valueOf(selectedHotelId));
        
        // Obtener token
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        if (token.isEmpty()) {
            txtErrorMessage.setText("No se encontró token de autenticación");
            return;
        }
        
        // Llamar al API
        ApiService apiService = ApiClient.getApiService();
        Call<JsonObject> call = apiService.registerHotelManager(userData);
        
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        Toast.makeText(RegisterHotelManagerActivity.this, 
                                "Gerente de hotel registrado correctamente", 
                                Toast.LENGTH_LONG).show();
                        finish(); // Volver a la pantalla anterior
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
