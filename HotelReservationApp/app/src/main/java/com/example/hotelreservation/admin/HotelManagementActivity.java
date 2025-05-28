package com.example.hotelreservation.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hotelreservation.MainActivity;
import com.example.hotelreservation.R;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.constraintlayout.widget.ConstraintLayout;

public class HotelManagementActivity extends AppCompatActivity {
    
    private static final String TAG = "HotelManagement";
    private LinearLayout hotelsContainer;
    private ProgressBar progressBar;
    private ApiService apiService;
    private FloatingActionButton fabAddHotel;
    private TextView tvErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_management);
        
        // Inicializar componentes de UI
        hotelsContainer = findViewById(R.id.hotelsContainer);
        progressBar = findViewById(R.id.progressBar);
        fabAddHotel = findViewById(R.id.fabAddHotel);
        
        // Buscar o crear un TextView para mensajes de error
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        if (tvErrorMessage == null) {
            tvErrorMessage = new TextView(this);
            tvErrorMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            // Añadirlo al layout principal - usando ConstraintLayout correctamente
            ConstraintLayout mainLayout = findViewById(R.id.mainContainer);
            if (mainLayout != null) {
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                // Colocar debajo del título si existe
                TextView title = findViewById(R.id.tvTitle);
                if (title != null) {
                    params.topToBottom = title.getId();
                } else {
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                }
                params.leftMargin = 16;
                params.rightMargin = 16;
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                tvErrorMessage.setLayoutParams(params);
                mainLayout.addView(tvErrorMessage);
            }
        }
        
        // Configurar API
        apiService = ApiClient.getApiService();
        
        // Verificar token antes de cargar datos
        validateToken();
        
        // Configurar botón para añadir hotel
        fabAddHotel.setOnClickListener(v -> showAddHotelDialog());
        
        // Añadir un botón para recargar los datos
        Button btnReload = findViewById(R.id.btnReloadHotels);
        if (btnReload != null) {
            btnReload.setOnClickListener(v -> {
                Toast.makeText(this, "Recargando datos...", Toast.LENGTH_SHORT).show();
                loadHotels();
            });
        } else {            // Si no existe el botón en el layout, crearlo programáticamente
            Button reloadButton = new Button(this);
            reloadButton.setText("Recargar datos");
            reloadButton.setOnClickListener(v -> {
                Toast.makeText(this, "Recargando datos...", Toast.LENGTH_SHORT).show();
                loadHotels();
            });
            
            // Añadirlo al layout principal - corregido para ConstraintLayout
            ConstraintLayout mainLayout = findViewById(R.id.mainContainer);
            if (mainLayout != null) {
                // Configurar parámetros del ConstraintLayout
                ConstraintLayout.LayoutParams params = 
                    new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    );
                
                // Posición relativa a otros elementos
                if (tvErrorMessage != null && tvErrorMessage.getParent() != null) {
                    params.topToBottom = tvErrorMessage.getId();
                } else {
                    TextView title = findViewById(R.id.tvTitle);
                    if (title != null) {
                        params.topToBottom = title.getId();
                    } else {
                        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.topMargin = 16;
                    }
                }
                
                params.leftMargin = 16;
                params.rightMargin = 16;
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                
                reloadButton.setLayoutParams(params);
                mainLayout.addView(reloadButton);
            }
        }
          // Añadir botón para cerrar sesión y volver a la pantalla de login
        Button btnLogout = new Button(this);
        btnLogout.setText("Cerrar sesión");
        btnLogout.setOnClickListener(v -> {
            // Limpiar preferencias y volver a login
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
          // Añadirlo al layout principal - usando ConstraintLayout correctamente
        ConstraintLayout mainLayout = findViewById(R.id.mainContainer);
        if (mainLayout != null) {
            // Configurar los parámetros de layout para el botón
            ConstraintLayout.LayoutParams params = 
                new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
            
            // Colocar el botón en la parte inferior del layout
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomMargin = 16;
            params.leftMargin = 16;
            params.rightMargin = 16;
            
            btnLogout.setLayoutParams(params);
            mainLayout.addView(btnLogout);
        }
    }
    
    private void validateToken() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        if (token.isEmpty()) {
            showError("No hay token de autenticación. Por favor, inicie sesión nuevamente.");
            return;
        }
        
        // Si hay token, cargar datos
        loadHotels();
    }

    private void loadHotels() {
        progressBar.setVisibility(View.VISIBLE);
        hotelsContainer.removeAllViews();
        tvErrorMessage.setText(""); // Limpiar mensajes de error anteriores
        
        // Obtener token de autorización
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        // Mostrar información de depuración
        Log.d(TAG, "Token utilizado: " + token);
        
        if (token.isEmpty()) {
            showError("No se encontró token de autenticación");
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        // Formato correcto del token para la cabecera Authorization - Asegurar que tiene el prefijo Bearer
        String authHeader = token;
        if (!token.startsWith("Bearer ")) {
            authHeader = "Bearer " + token;
        }
        Log.d(TAG, "Cabecera Authorization: " + authHeader);
        
        // Hacer llamada a la API para obtener hoteles
        Call<List<String>> call = apiService.listarHoteles(authHeader);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<String> hotels = response.body();
                    if (hotels.isEmpty()) {
                        showEmptyMessage();
                    } else {
                        for (int i = 0; i < hotels.size(); i++) {
                            // Si tu backend devuelve solo nombres, puedes mostrar el nombre
                            addHotelView(i + 1, hotels.get(i), "");
                        }
                    }
                } else {
                    showError("Error al obtener hoteles: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Error de red: " + t.getMessage());
            }
        });
    }
    
    private void showEmptyMessage() {
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("No hay hoteles registrados");
        tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvEmpty.setPadding(0, 50, 0, 0);
        hotelsContainer.addView(tvEmpty);
    }
    
    private void addHotelView(int id, String name, String address) {
        View hotelView = LayoutInflater.from(this).inflate(R.layout.item_hotel, null);
        TextView tvHotelName = hotelView.findViewById(R.id.tvHotelName);
        TextView tvHotelAddress = hotelView.findViewById(R.id.tvHotelAddress);
        tvHotelName.setText(name);
        tvHotelAddress.setText(address);
        hotelView.setTag(id);
        // Eliminar botón de editar
        hotelView.findViewById(R.id.btnDeleteHotel).setOnClickListener(v -> {
            showDeleteConfirmation(id);
        });
        hotelsContainer.addView(hotelView);
    }
    
    private void showAddHotelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_hotel, null);
        builder.setView(dialogView);
        
        EditText etHotelName = dialogView.findViewById(R.id.etHotelName);
        EditText etHotelAddress = dialogView.findViewById(R.id.etHotelAddress);
        
        builder.setTitle("Añadir Hotel")
               .setPositiveButton("Guardar", (dialog, which) -> {
                   String name = etHotelName.getText().toString().trim();
                   String address = etHotelAddress.getText().toString().trim();
                   
                   if (name.isEmpty() || address.isEmpty()) {
                       Toast.makeText(HotelManagementActivity.this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   addHotel(name, address);
               })
               .setNegativeButton("Cancelar", null)
               .create()
               .show();
    }

    private void addHotel(String name, String address) {
        progressBar.setVisibility(View.VISIBLE);
        try {
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String token = preferences.getString("token", "");
            String authHeader = "Bearer " + token;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("nombre", name); // Backend espera 'nombre'
            jsonObject.put("direccion", address); // Backend espera 'direccion'
            RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiService.createHotel(authHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(HotelManagementActivity.this, "Hotel creado con éxito", Toast.LENGTH_SHORT).show();
                        loadHotels();
                    } else {
                        showError("Error al crear hotel: " + response.code());
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    showError("Error de conexión: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            showError("Error: " + e.getMessage());
        }
    }

    private void updateHotel(int id, String name, String address) {
        progressBar.setVisibility(View.VISIBLE);
        try {
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String token = preferences.getString("token", "");
            String authHeader = "Bearer " + token;
            Log.d("HotelManagement", "Update Hotel - Token: " + authHeader);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("address", address);
            RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiService.updateHotel(authHeader, id, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(HotelManagementActivity.this, "Hotel actualizado con éxito", Toast.LENGTH_SHORT).show();
                        loadHotels();
                    } else {
                        showError("Error al actualizar hotel: " + response.code());
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    showError("Error de conexión: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            showError("Error: " + e.getMessage());
        }
    }
    
    private void showDeleteConfirmation(int id) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar Hotel")
            .setMessage("¿Está seguro que desea eliminar este hotel? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> deleteHotel(id))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteHotel(int id) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Obtener token de autorización
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", "");
        
        // Formato correcto del token
        String authHeader = "Bearer " + token;
        Log.d("HotelManagement", "Delete Hotel - Token: " + authHeader);
        
        Map<String, Integer> params = new HashMap<>();
        params.put("id", id);
        
        Call<ResponseBody> call = apiService.deleteHotel(authHeader, params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(HotelManagementActivity.this, "Hotel eliminado con éxito", Toast.LENGTH_SHORT).show();
                    loadHotels(); // Recargar lista
                } else {
                    showError("Error al eliminar hotel: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    private void showError(String message) {
        tvErrorMessage.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
