package com.example.hotelreservation.reservation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hotelreservation.R;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationActivity extends AppCompatActivity {

    private LinearLayout reservationsContainer;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyView;
    private ProgressBar progressBar;
    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // Inicializar formateadores de fecha
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Inicializar vistas
        Toolbar toolbar = findViewById(R.id.toolbar);

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
    }

    private void cargarReservas() {
        showLoading();

        ApiService apiService = ApiClient.getApiService();
        // Obtener ID de usuario y el token con de SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = preferences.getInt("USER_ID", -1);
        String token = preferences.getString("token", null);
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }
        if (token != null) {
            // Aquí puedes usar el token para tus peticiones o validaciones
            Log.d("Token", "Token recuperado: " + token);
        } else {
            // Si el token no existe, puede que el usuario no haya iniciado sesión o el token haya expirado
            Log.d("Token", "No hay token guardado.");
        }
        Call<ResponseBody> call = apiService.listarReservas("Bearer "+token,userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONArray reservationsArray = new JSONArray(jsonString);
                        handleReservationsResponse(reservationsArray);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError();
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                showError();
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
    }

    private void añadirReserva(JSONObject reservation) throws JSONException {
        // Inflar la vista del item de reserva
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_reservation, reservationsContainer, false);

        // Obtener referencias a los TextView
        TextView tvHotelName = itemView.findViewById(R.id.tvHotelName);
        TextView tvRoomType = itemView.findViewById(R.id.tvRoomType);
        TextView tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
        TextView tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
        TextView tvPrice = itemView.findViewById(R.id.tvPrice);
        TextView tvStatus = itemView.findViewById(R.id.tvStatus);

        // Obtener datos de la reserva
        JSONObject habitacion = reservation.getJSONObject("habitacion");
        String roomNumber = habitacion.getString("nuemro");
        String floor = habitacion.getString("planta");
        int maxGuests = habitacion.getInt("nmax");
        String hotelName = habitacion.getJSONObject("hotel").getString("nombre");
        int cantidadPersonas = reservation.getInt("cantidad");

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
        tvRoomType.setText(roomInfo);

        tvCheckInDate.setText(checkInDate);
        tvCheckOutDate.setText(checkOutDate);
        tvPrice.setText(cantidadPersonas + " " + (cantidadPersonas == 1 ? "persona" : "personas"));
        tvStatus.setText("Confirmado");

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

    private void showError() {
        hideLoading();
        // Implementar manejo de error (podría mostrar un Toast o Snackbar)
    }
}