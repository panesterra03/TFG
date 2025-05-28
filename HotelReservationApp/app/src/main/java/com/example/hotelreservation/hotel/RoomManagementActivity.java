package com.example.hotelreservation.hotel;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hotelreservation.R;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomManagementActivity extends AppCompatActivity {

    private static final String TAG = "RoomManagementActivity";
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout roomsContainer;
    private ProgressBar progressBar;
    private TextView emptyView;
    private String hotelName;
    private int hotelId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        // Inicializar vistas
        swipeRefreshLayout = findViewById(R.id.swipeRefreshRooms);
        roomsContainer = findViewById(R.id.roomsContainer);
        progressBar = findViewById(R.id.progressBarRooms);
        emptyView = findViewById(R.id.emptyViewRooms);
        FloatingActionButton fabAddRoom = findViewById(R.id.fabAddRoom);

        // Obtener datos de la sesión
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        token = preferences.getString("token", "");
        hotelName = preferences.getString("HOTEL_NAME", "");
        hotelId = preferences.getInt("HOTEL_ID", -1);

        // Configurar título
        TextView tvTitle = findViewById(R.id.tvRoomManagementTitle);
        tvTitle.setText("Habitaciones del Hotel " + hotelName);

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadRooms);

        // Configurar botón para añadir habitación
        fabAddRoom.setOnClickListener(v -> showAddRoomDialog());

        // Cargar habitaciones
        loadRooms();
    }

    private void loadRooms() {
        if (token.isEmpty() || hotelName.isEmpty()) {
            Toast.makeText(this, "Error: Datos de sesión incompletos", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        showLoading();

        ApiService apiService = ApiClient.getApiService();
        Call<List<Map<String, Object>>> call = apiService.listarHabitaciones("Bearer " + token, hotelName);

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> rooms = response.body();
                    if (rooms.isEmpty()) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                        displayRooms(rooms);
                    }
                } else {
                    showError("Error al cargar habitaciones: " + response.message());
                }
                
                hideLoading();
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showError("Error de red: " + t.getMessage());
                hideLoading();
            }
        });
    }

    private void displayRooms(List<Map<String, Object>> rooms) {
        roomsContainer.removeAllViews();
        
        LayoutInflater inflater = LayoutInflater.from(this);
        
        for (Map<String, Object> room : rooms) {
            CardView cardView = (CardView) inflater.inflate(R.layout.item_room, roomsContainer, false);
            
            TextView tvRoomNumber = cardView.findViewById(R.id.tvRoomNumber);
            TextView tvRoomFloor = cardView.findViewById(R.id.tvRoomFloor);
            TextView tvRoomCapacity = cardView.findViewById(R.id.tvRoomCapacity);
            ImageButton btnDeleteRoom = cardView.findViewById(R.id.btnDeleteRoom);
            
            // Configurar vistas con datos de la habitación
            String roomNumber = String.valueOf(room.get("numero"));
            String roomFloor = String.valueOf(room.get("planta"));
            int roomCapacity = Integer.parseInt(String.valueOf(room.get("nmax")));
            Number roomId = (Number) room.get("id");
            
            tvRoomNumber.setText("Habitación: " + roomNumber);
            tvRoomFloor.setText("Planta: " + roomFloor);
            tvRoomCapacity.setText("Capacidad: " + roomCapacity + " personas");
            
            // Configurar botón de eliminar
            btnDeleteRoom.setOnClickListener(v -> confirmDeleteRoom(roomId.longValue(), roomNumber));
            
            // Añadir la tarjeta al contenedor
            roomsContainer.addView(cardView);
        }
    }

    private void confirmDeleteRoom(long roomId, String roomNumber) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Habitación")
                .setMessage("¿Está seguro que desea eliminar la habitación " + roomNumber + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteRoom(roomId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteRoom(long roomId) {
        if (token.isEmpty()) {
            Toast.makeText(this, "Error: No hay token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading();
        
        Map<String, String> data = new HashMap<>();
        data.put("habitacionId", String.valueOf(roomId));
        
        ApiService apiService = ApiClient.getApiService();
        Call<JsonObject> call = apiService.eliminarHabitacion("Bearer " + token, data);
        
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = response.body();
                    
                    if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                        Toast.makeText(RoomManagementActivity.this, 
                                "Habitación eliminada correctamente", Toast.LENGTH_SHORT).show();
                        loadRooms(); // Recargar la lista
                    } else {
                        String message = jsonResponse.has("message") ? 
                                jsonResponse.get("message").getAsString() : "Error al eliminar la habitación";
                        Toast.makeText(RoomManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RoomManagementActivity.this, 
                            "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideLoading();
                Toast.makeText(RoomManagementActivity.this, 
                        "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        builder.setView(dialogView);
        
        EditText edtRoomNumber = dialogView.findViewById(R.id.edtRoomNumber);
        EditText edtRoomFloor = dialogView.findViewById(R.id.edtRoomFloor);
        EditText edtRoomCapacity = dialogView.findViewById(R.id.edtRoomCapacity);
        Button btnAddRoom = dialogView.findViewById(R.id.btnDialogAddRoom);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        
        AlertDialog dialog = builder.create();
        
        btnAddRoom.setOnClickListener(v -> {
            String roomNumber = edtRoomNumber.getText().toString().trim();
            String roomFloor = edtRoomFloor.getText().toString().trim();
            String roomCapacity = edtRoomCapacity.getText().toString().trim();
            
            if (roomNumber.isEmpty() || roomFloor.isEmpty() || roomCapacity.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (hotelId <= 0) {
                Toast.makeText(this, "Error: ID de hotel no válido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            dialog.dismiss();
            addRoom(roomNumber, roomFloor, roomCapacity, hotelId);
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void addRoom(String roomNumber, String roomFloor, String roomCapacity, int hotelId) {
        if (token.isEmpty()) {
            Toast.makeText(this, "Error: No hay token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading();
        
        Map<String, String> data = new HashMap<>();
        data.put("numero", roomNumber);
        data.put("planta", roomFloor);
        data.put("nmax", roomCapacity);
        data.put("hotelId", String.valueOf(hotelId));
        
        ApiService apiService = ApiClient.getApiService();
        Call<JsonObject> call = apiService.crearHabitacion("Bearer " + token, data);
        
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RoomManagementActivity.this, 
                            "Habitación creada correctamente", Toast.LENGTH_SHORT).show();
                    loadRooms(); // Recargar la lista
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    
                    Toast.makeText(RoomManagementActivity.this, 
                            "Error: " + (errorBody.isEmpty() ? response.message() : errorBody), 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideLoading();
                Toast.makeText(RoomManagementActivity.this, 
                        "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        roomsContainer.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        roomsContainer.setVisibility(View.VISIBLE);
    }

    private void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        roomsContainer.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        roomsContainer.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        emptyView.setText("Error: " + message);
        showEmptyView();
    }
}
