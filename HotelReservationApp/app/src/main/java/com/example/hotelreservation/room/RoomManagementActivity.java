package com.example.hotelreservation.room;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hotelreservation.R;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.constraintlayout.widget.ConstraintLayout;

public class RoomManagementActivity extends AppCompatActivity {

    private LinearLayout recyclerView; // Cambiado a LinearLayout para coincidir con el layout
    private ProgressBar progressBar;
    private TextView emptyView;
    private FloatingActionButton fabAddRoom;
    private Spinner spinnerHotelFilter;
    private List<String> hotelList = new ArrayList<>();
    private ArrayAdapter<String> hotelAdapter;
    private List<Map<String, Object>> allRooms = new ArrayList<>();
    private List<Map<String, Object>> filteredRooms = new ArrayList<>();
    private ApiService apiService;    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        // Inicializar la barra de herramientas
        // Nota: No hay toolbar en el layout actual, usaremos tvRoomManagementTitle en su lugar
        // como título de la actividad
        setTitle("Gestión de Habitaciones");

        // Inicializar vistas - usar los ids correctos que existen en el layout
        recyclerView = findViewById(R.id.roomsContainer); // LinearLayout en lugar de RecyclerView
        progressBar = findViewById(R.id.progressBarRooms);
        emptyView = findViewById(R.id.emptyViewRooms);
        fabAddRoom = findViewById(R.id.fabAddRoom);        
        spinnerHotelFilter = new Spinner(this);
        hotelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hotelList);
        hotelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHotelFilter.setAdapter(hotelAdapter);
        // Añadir el spinner al layout (arriba del contenedor de habitaciones)
        ConstraintLayout mainLayout = findViewById(R.id.swipeRefreshRooms).getParent() instanceof ConstraintLayout ? (ConstraintLayout) findViewById(R.id.swipeRefreshRooms).getParent() : null;
        if (mainLayout != null) {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params.topToBottom = R.id.tvRoomManagementTitle;
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            spinnerHotelFilter.setLayoutParams(params);
            mainLayout.addView(spinnerHotelFilter, 1); // Insertar después del título
        }
        // El contenedor es un LinearLayout, no un RecyclerView
        // Por lo que no necesitamos configurar un LayoutManager

        // Inicializar API service
        apiService = ApiClient.getApiService();

        // Configurar el botón de agregar habitación
        fabAddRoom.setOnClickListener(view -> showAddRoomDialog());

        // Cargar hoteles en el filtro
        String token = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("token", "");
        apiService.listarHoteles("Bearer " + token).enqueue(new retrofit2.Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hotelList.clear();
                    hotelList.add("Todos los hoteles");
                    hotelList.addAll(response.body());
                    hotelAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(RoomManagementActivity.this, "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
            }
        });
        spinnerHotelFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterRoomsByHotel();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Cargar habitaciones
        loadRooms();
    }    private void loadRooms() {
        showLoading();
        String token = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("token", "");
        // Obtener el rol del usuario de las preferencias o del intent
        String userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) {
            userRole = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("USER_ROLE", "");
        }
        
        Log.d("RoomManagementActivity", "Cargando habitaciones con rol: " + userRole);
        
        // Si es administrador, cargar habitaciones de todos los hoteles
        if ("admin".equals(userRole)) {
            loadAllHotelsRooms(token);
        } else {
            // Para otros roles, intentar cargar todas las habitaciones directamente
            apiService.listarHabitaciones("Bearer " + token, "").enqueue(new retrofit2.Callback<List<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                    hideLoading();
                    if (response.isSuccessful() && response.body() != null) {
                        allRooms.clear();
                        allRooms.addAll(response.body());
                        filterRoomsByHotel();
                    } else {
                        showEmptyView();
                    }
                }
                @Override
                public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                    hideLoading();
                    showEmptyView();
                }
            });
        }
    }
    
    private void loadAllHotelsRooms(String token) {
        // Primero obtenemos la lista de hoteles
        apiService.listarHoteles("Bearer " + token).enqueue(new retrofit2.Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRooms.clear();
                    List<String> hotels = response.body();
                    
                    if (hotels.isEmpty()) {
                        hideLoading();
                        showEmptyView();
                        return;
                    }
                    
                    // Contador para seguir cuántos hoteles hemos procesado
                    final int[] hotelsProcessed = {0};
                    
                    // Para cada hotel, cargar sus habitaciones
                    for (String hotel : hotels) {
                        apiService.listarHabitaciones("Bearer " + token, hotel).enqueue(new retrofit2.Callback<List<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                                hotelsProcessed[0]++;
                                
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Map<String, Object>> rooms = response.body();
                                    // Añadir el nombre del hotel a cada habitación
                                    for (Map<String, Object> room : rooms) {
                                        room.put("hotel", hotel);
                                        allRooms.add(room);
                                    }
                                }
                                
                                // Si hemos procesado todos los hoteles, filtrar y mostrar
                                if (hotelsProcessed[0] >= hotels.size()) {
                                    hideLoading();
                                    filterRoomsByHotel();
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                                hotelsProcessed[0]++;
                                
                                // Si hemos procesado todos los hoteles, filtrar y mostrar
                                if (hotelsProcessed[0] >= hotels.size()) {
                                    hideLoading();
                                    filterRoomsByHotel();
                                }
                            }
                        });
                    }
                } else {
                    hideLoading();
                    showEmptyView();
                }
            }
              @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                hideLoading();
                showEmptyView();                Toast.makeText(RoomManagementActivity.this, "Error al cargar los hoteles", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void filterRoomsByHotel() {
        String selectedHotel = spinnerHotelFilter.getSelectedItem() != null ? spinnerHotelFilter.getSelectedItem().toString() : "";
        filteredRooms.clear();
        
        if (selectedHotel.equals("Todos los hoteles") || selectedHotel.isEmpty()) {
            filteredRooms.addAll(allRooms);
        } else {
            for (Map<String, Object> room : allRooms) {
                if (room.containsKey("hotel") && selectedHotel.equals(room.get("hotel"))) {
                    filteredRooms.add(room);
                }
            }
        }
        
        // Registrar el número de habitaciones encontradas para depuración
        Log.d("RoomManagement", "Habitaciones filtradas: " + filteredRooms.size() + 
              " de " + allRooms.size() + " totales. Filtro: " + selectedHotel);
        
        showRooms();
    }    private void showRooms() {
        recyclerView.removeAllViews();
        if (filteredRooms.isEmpty()) {
            showEmptyView();
            return;
        }
        
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        for (Map<String, Object> room : filteredRooms) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_room, null);
            TextView tvRoomNumber = item.findViewById(R.id.tvRoomNumber);
            TextView tvRoomFloor = item.findViewById(R.id.tvRoomFloor);
            TextView tvRoomCapacity = item.findViewById(R.id.tvRoomCapacity);
            TextView tvRoomHotel = item.findViewById(R.id.tvRoomHotel);
            
            // Obtener valores de forma segura
            String roomNumber = room.get("numero") != null ? room.get("numero").toString() : "N/A";
            String roomFloor = room.get("planta") != null ? room.get("planta").toString() : "N/A";
            String roomCapacity = room.get("nmax") != null ? room.get("nmax").toString() : "N/A";
            String hotelName = room.get("hotel") != null ? room.get("hotel").toString() : "Hotel desconocido";
            
            // Establecer valores en las vistas
            tvRoomNumber.setText("Habitación: " + roomNumber);
            tvRoomFloor.setText("Planta: " + roomFloor);
            tvRoomCapacity.setText("Capacidad: " + roomCapacity + " personas");
            tvRoomHotel.setText("Hotel: " + hotelName);
            
            // Registrar para depuración
            Log.d("RoomDetails", "Mostrando habitación: " + roomNumber + " del hotel: " + hotelName);
            
            recyclerView.addView(item);
        }
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir Nueva Habitación");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_room, null);
        builder.setView(dialogView);

        EditText etRoomNumber = dialogView.findViewById(R.id.edtRoomNumber);
        EditText etRoomFloor = dialogView.findViewById(R.id.edtRoomFloor);
        EditText etRoomCapacity = dialogView.findViewById(R.id.edtRoomCapacity);
        Spinner spinnerHotels = dialogView.findViewById(R.id.spinnerHotels);
        Button btnAdd = dialogView.findViewById(R.id.btnDialogAddRoom);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);

        // Cargar hoteles en el spinner
        List<String> hotelList = new ArrayList<>();
        List<Long> hotelIdList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hotelList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHotels.setAdapter(adapter);        // Obtener token y cargar hoteles
        String token = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("token", "");
        ApiService apiService = ApiClient.getApiService();
        apiService.listarHoteles("Bearer " + token).enqueue(new retrofit2.Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hotelList.clear();
                    hotelIdList.clear();
                    for (String hotelNombre : response.body()) {
                        hotelList.add(hotelNombre);
                    }
                    adapter.notifyDataSetChanged();
                    // Añadir mensaje de depuración para ver los hoteles cargados
                    Log.d("HotelData", "Hoteles cargados: " + hotelList.toString());
                }
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(RoomManagementActivity.this, "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
            }
        });

        // Cambia la carga de hoteles para obtener los IDs reales desde el backend
        // y asócialos correctamente al spinner
        // Suponiendo que la API solo devuelve nombres, necesitas una API que devuelva también IDs
        // Por ahora, muestra un mensaje claro si no puedes asociar el ID real
        // Si tienes una API que devuelve List<Map<String, Object>> con id y nombre, úsala aquí
        // Ejemplo de cómo debería ser:
        // apiService.listarHotelesConId("Bearer " + token).enqueue(new Callback<List<Map<String, Object>>>() {
        //     @Override
        //     public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
        //         if (response.isSuccessful() && response.body() != null) {
        //             hotelList.clear();
        //             hotelIdList.clear();
        //             for (Map<String, Object> hotel : response.body()) {
        //                 hotelList.add((String) hotel.get("nombre"));
        //                 hotelIdList.add(Long.parseLong(hotel.get("id").toString()));
        //             }
        //             adapter.notifyDataSetChanged();
        //         }
        //     }
        //     ...
        // });
        // Si no tienes esa API, muestra un Toast de advertencia:
        // Toast.makeText(RoomManagementActivity.this, "No se pueden asociar los IDs reales de los hoteles. Contacta con el administrador.", Toast.LENGTH_LONG).show();

        AlertDialog dialog = builder.create();

        btnAdd.setOnClickListener(v -> {            String roomNumber = etRoomNumber.getText().toString().trim();
            String roomFloor = etRoomFloor.getText().toString().trim();
            String roomCapacity = etRoomCapacity.getText().toString().trim();
            int selectedHotelIndex = spinnerHotels.getSelectedItemPosition();
            String selectedHotelName = spinnerHotels.getSelectedItem() != null ? spinnerHotels.getSelectedItem().toString() : ""; // Obtener el NOMBRE del hotel seleccionado
            if (roomNumber.isEmpty() || roomFloor.isEmpty() || roomCapacity.isEmpty() || selectedHotelIndex < 0 || selectedHotelName.isEmpty()) {
                Toast.makeText(RoomManagementActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> data = new java.util.HashMap<>();
            data.put("numero", roomNumber);
            data.put("planta", roomFloor);
            data.put("nmax", roomCapacity);
            data.put("hotelId", selectedHotelName); // Enviar el NOMBRE del hotel como hotelId
            Call<JsonObject> call = apiService.crearHabitacion("Bearer " + token, data);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RoomManagementActivity.this, "Habitación añadida correctamente", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRooms();
                    } else {
                        Toast.makeText(RoomManagementActivity.this, "Error al añadir habitación", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(RoomManagementActivity.this, "Error de red al añadir habitación", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
