package com.example.hotelreservation.reservation;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hotelreservation.R;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationActivity extends AppCompatActivity {

    private LinearLayout reservationsContainer;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyView;
    private ProgressBar progressBar;    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;
    private TextView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // Inicializar formateadores de fecha
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Inicializar vistas
        Toolbar toolbar = findViewById(R.id.toolbar);
        errorView = findViewById(R.id.errorView);

        // Reemplazar el RecyclerView con un LinearLayout para contener las vistas de reserva
        reservationsContainer = new LinearLayout(this);
        reservationsContainer.setOrientation(LinearLayout.VERTICAL);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.removeAllViews();
        swipeRefresh.addView(reservationsContainer);

        emptyView = findViewById(R.id.emptyView);
        progressBar = findViewById(R.id.progressBar);

        // Configurar SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::cargarReservas);

        // Cargar reservas al iniciar
        cargarReservas();
    }    private void cargarReservas() {
        showLoading();

        ApiService apiService = ApiClient.getApiService();
        // Obtener ID de usuario y el token de SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = preferences.getInt("USER_ID", -1);
        String token = preferences.getString("token", null);
        String userRole = preferences.getString("USER_ROLE", "user");
        
        if (token == null) {
            Toast.makeText(this, "Error: No hay token de autenticación", Toast.LENGTH_SHORT).show();
            hideLoading();
            return;
        }
        
        // Verificar si es administrador (por la bandera pasada por Intent o por el rol del usuario)
        boolean isAdmin = "admin".equals(userRole) || getIntent().getBooleanExtra("IS_ADMIN", false);
        
        // Verificar si el usuario es hotel manager y se debe mostrar todas las reservas del hotel
        boolean isHotelManager = "hotelmanager".equals(userRole);
        int hotelId = preferences.getInt("HOTEL_ID", -1);
        
        // Verificar si se recibió el ID del hotel desde la actividad anterior
        if (getIntent().getBooleanExtra("IS_HOTEL_MANAGER", false)) {
            isHotelManager = true;
            hotelId = getIntent().getIntExtra("HOTEL_ID", hotelId);
        }
        
        Log.d("ReservationActivity", "Cargando reservas - Role: " + userRole + ", Admin: " + isAdmin + ", HotelManager: " + isHotelManager);

        // Si es administrador, cargar todas las reservas de todos los hoteles
        if (isAdmin) {
            cargarTodasLasReservas(token);
        }
        // Si es hotel manager y tiene un hotel asignado, cargar todas las reservas del hotel
        else if (isHotelManager && hotelId > 0) {
            Call<ResponseBody> call = apiService.listarReservasPorHotel("Bearer " + token, hotelId);
            
            // Actualizamos el título de la actividad
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle("Reservas del Hotel");
            }
            
            fetchReservations(call);
        } else {
            // Si es usuario normal, cargar solo sus reservas
            if (userId == -1) {
                Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
                hideLoading();
                return;
            }
            
            Call<ResponseBody> call = apiService.listarReservas("Bearer " + token, userId);
            fetchReservations(call);
        }
    }
    
    // Método para cargar todas las reservas cuando el usuario es administrador
    private void cargarTodasLasReservas(String token) {
        // Primero, obtenemos la lista de todos los hoteles
        ApiService apiService = ApiClient.getApiService();
        apiService.listarHoteles("Bearer " + token).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> hoteles = response.body();
                    if (hoteles.isEmpty()) {
                        hideLoading();
                        showEmptyState();
                        return;
                    }
                    
                    // Actualizar el título
                    Toolbar toolbar = findViewById(R.id.toolbar);
                    if (toolbar != null) {
                        toolbar.setTitle("Todas las Reservas");
                    }
                    
                    // Para almacenar todas las reservas
                    JSONArray todasLasReservas = new JSONArray();
                    final int[] hotelesProcessed = {0};
                    
                    // Para cada hotel, obtenemos sus reservas
                    for (int i = 0; i < hoteles.size(); i++) {
                        final int hotelIndex = i;
                        try {
                            // Obtener el ID del hotel - como solo tenemos nombres, usamos el índice como ID
                            // Esto es una aproximación que funciona si la API no proporciona IDs
                            final int hotelId = hotelIndex + 1; // IDs empezando desde 1
                            
                            apiService.listarReservasPorHotel("Bearer " + token, hotelId).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    hotelesProcessed[0]++;
                                    
                                    if (response.isSuccessful() && response.body() != null) {
                                        try {
                                            String jsonString = response.body().string();
                                            JSONArray reservasHotel = new JSONArray(jsonString);
                                            
                                            // Añadir las reservas de este hotel a la lista completa
                                            for (int j = 0; j < reservasHotel.length(); j++) {
                                                todasLasReservas.put(reservasHotel.getJSONObject(j));
                                            }
                                            
                                            Log.d("ReservationActivity", "Reservas de hotel " + hoteles.get(hotelIndex) + 
                                                  ": " + reservasHotel.length() + " - Total acumulado: " + todasLasReservas.length());
                                        } catch (Exception e) {
                                            Log.e("ReservationActivity", "Error procesando reservas del hotel " + 
                                                  hoteles.get(hotelIndex) + ": " + e.getMessage());
                                        }
                                    }
                                    
                                    // Si hemos procesado todos los hoteles, mostrar las reservas
                                    if (hotelesProcessed[0] >= hoteles.size()) {
                                        Log.d("ReservationActivity", "Procesados todos los hoteles. Total reservas: " + 
                                              todasLasReservas.length());
                                        handleReservationsResponse(todasLasReservas);
                                    }
                                }
                                
                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    hotelesProcessed[0]++;
                                    Log.e("ReservationActivity", "Error al cargar reservas del hotel " + 
                                          hoteles.get(hotelIndex) + ": " + t.getMessage());
                                    
                                    // Si hemos procesado todos los hoteles, mostrar las reservas que tengamos
                                    if (hotelesProcessed[0] >= hoteles.size()) {
                                        handleReservationsResponse(todasLasReservas);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Log.e("ReservationActivity", "Error general al procesar hotel " + 
                                  hoteles.get(hotelIndex) + ": " + e.getMessage());
                            hotelesProcessed[0]++;
                            
                            // Si hemos procesado todos los hoteles, mostrar las reservas que tengamos
                            if (hotelesProcessed[0] >= hoteles.size()) {
                                handleReservationsResponse(todasLasReservas);
                            }
                        }
                    }
                } else {
                    hideLoading();
                    showErrorView("Error al cargar la lista de hoteles");
                }
            }
            
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                hideLoading();
                showErrorView("Error de red al cargar hoteles: " + t.getMessage());
            }
        });
    }
    
    private void fetchReservations(Call<ResponseBody> call) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONArray reservationsArray = new JSONArray(jsonString);
                        handleReservationsResponse(reservationsArray);
                    } catch (Exception e) {
                        Log.e("ReservationActivity", "Error parsing reservations: " + e.getMessage());
                        showErrorView("Error al procesar las reservas");
                    }
                } else {
                    Log.e("ReservationActivity", "Error en respuesta: " + response.code());
                    showErrorView("Error al cargar las reservas");
                }
                hideLoading();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ReservationActivity", "Error en la llamada: " + t.getMessage());
                showErrorView("Error de red: " + t.getMessage());
                hideLoading();
            }
        });
    }

    private void handleReservationsResponse(JSONArray reservationsArray) {
        hideLoading();

        // Limpiar contenedor de reservas
        reservationsContainer.removeAllViews();

        if (reservationsArray.length() == 0) {
            showEmptyState();
        } else {
            showReservations();

            // Iterar a través de cada reserva y crear una vista para cada una
            for (int i = 0; i < reservationsArray.length(); i++) {
                try {
                    JSONObject reservation = reservationsArray.getJSONObject(i);
                    añadirReserva(reservation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }    private void añadirReserva(JSONObject reservation) throws JSONException {
        // Inflar la vista del item de reserva
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_reservation, reservationsContainer, false);

        // Obtener referencias a los TextView
        TextView tvHotelName = itemView.findViewById(R.id.tvHotelName);
        TextView tvRoomType = itemView.findViewById(R.id.tvRoomType);
        TextView tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
        TextView tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
        TextView tvPrice = itemView.findViewById(R.id.tvPrice);
        TextView tvStatus = itemView.findViewById(R.id.tvStatus);
        Button btnCancelReservation = itemView.findViewById(R.id.btnCancelReservation);

        // Obtener datos de la reserva
        JSONObject habitacion = reservation.getJSONObject("habitacion");
        String roomNumber = habitacion.getString("nuemro");
        String floor = habitacion.getString("planta");
        int maxGuests = habitacion.getInt("nmax");
        String hotelName = habitacion.getJSONObject("hotel").getString("nombre");
        int cantidadPersonas = reservation.getInt("cantidad");
        
        // Obtener el ID de la reserva para poder cancelarla
        final long reservationId = reservation.getLong("id");
        
        // Intentar obtener información del usuario (si está disponible)
        String userInfo = "";
        try {
            if (reservation.has("usuario") && !reservation.isNull("usuario")) {
                JSONObject usuario = reservation.getJSONObject("usuario");
                if (usuario.has("nombre") && !usuario.isNull("nombre")) {
                    userInfo = "Usuario: " + usuario.getString("nombre");
                } else if (usuario.has("username") && !usuario.isNull("username")) {
                    userInfo = "Usuario: " + usuario.getString("username");
                }
            }
        } catch (Exception e) {
            Log.e("ReservationActivity", "Error al obtener información del usuario: " + e.getMessage());
        }

        // Formatear fechas
        String checkInDate = formatDate(reservation.getString("fechaInicio"));
        String checkOutDate = formatDate(reservation.getString("fechaFin"));

        // Establecer valores en los TextView
        tvHotelName.setText(hotelName);

        // Para el tipo de habitación, mostrar el número y planta (y opcionalmente max personas)
        String roomInfo = roomNumber + " - Planta " + floor;
        if (roomInfo.length() < 30) {  // Si no es muy largo, añadir capacidad máxima
            roomInfo += " - Máx. " + maxGuests + " personas";
        }
        if (!userInfo.isEmpty()) {
            roomInfo += "\n" + userInfo;
        }
        tvRoomType.setText(roomInfo);

        tvCheckInDate.setText(checkInDate);
        tvCheckOutDate.setText(checkOutDate);
        tvPrice.setText(cantidadPersonas + " " + (cantidadPersonas == 1 ? "persona" : "personas"));
        tvStatus.setText("Confirmado");

        // Configurar el botón de cancelar reserva
        btnCancelReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar diálogo de confirmación
                new AlertDialog.Builder(ReservationActivity.this)
                    .setTitle("Cancelar Reserva")
                    .setMessage("¿Estás seguro de que deseas cancelar esta reserva?")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelarReserva(reservationId);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            }
        });

        // Agregar la vista al contenedor
        reservationsContainer.addView(itemView);
    }

    private String formatDate(String apiDate) {
        try {
            Date date = apiDateFormat.parse(apiDate);
            return displayDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return apiDate; // Devolver original si falla el análisis
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        reservationsContainer.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showReservations() {
        reservationsContainer.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        reservationsContainer.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    /**
     * Muestra un mensaje de error al usuario
     * @param message El mensaje de error a mostrar
     */
    private void showErrorView(String message) {
        // Si tenemos una vista de error específica, la podemos mostrar
        if (errorView != null) {
            errorView.setVisibility(View.VISIBLE);
            errorView.setText(message);
        } else {
            // Si no, usamos un Toast como alternativa
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        
        // Ocultar la vista de carga
        hideLoading();
        
        // Mostrar la vista vacía si no hay reservas
        if (reservationsContainer != null && reservationsContainer.getChildCount() == 0) {
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Cancela una reserva utilizando el endpoint correspondiente
     * @param reservationId ID de la reserva a cancelar
     */    private void cancelarReserva(long reservationId) {
        showLoading();
        
        ApiService apiService = ApiClient.getApiService();
        String token = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("token", "");
        
        if (token == null || token.isEmpty()) {
            hideLoading();
            Toast.makeText(this, "Error: No hay token de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Llamar al endpoint de cancelación pasando el ID en la URL
        apiService.borrarReserva("Bearer " + token, reservationId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ReservationActivity.this, "Reserva cancelada correctamente", Toast.LENGTH_SHORT).show();
                    
                    // Recargar las reservas para actualizar la vista
                    cargarReservas();
                } else {
                    try {
                        String errorMessage = "Error al cancelar la reserva";
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                        Toast.makeText(ReservationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ReservationActivity.this, "Error al cancelar la reserva", Toast.LENGTH_LONG).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideLoading();
                Toast.makeText(ReservationActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}