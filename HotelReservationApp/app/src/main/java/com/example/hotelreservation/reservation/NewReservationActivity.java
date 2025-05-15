package com.example.hotelreservation.reservation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.hotelreservation.R;
import com.example.hotelreservation.api.ApiClient;
import com.example.hotelreservation.api.ApiService;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import android.content.SharedPreferences;

public class NewReservationActivity extends AppCompatActivity {
    private Spinner spinnerHotel;
    private Spinner spinnerHabitacion;
    private DatePicker datePickerCheckIn;
    private DatePicker datePickerCheckOut;
    private EditText edtGuests;
    private Button btnCreateReservation;
    private List<Map<String, Object>> habitacionesList; // Para almacenar la información de las habitaciones

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation);

        // Inicializar vistas
        spinnerHotel = findViewById(R.id.spinnerHotel);
        spinnerHabitacion = findViewById(R.id.spinnerRoom);
        datePickerCheckIn = findViewById(R.id.datePickerCheckIn);
        datePickerCheckOut = findViewById(R.id.datePickerCheckOut);
        edtGuests = findViewById(R.id.edtGuests);
        btnCreateReservation = findViewById(R.id.btnCreateReservation);
        habitacionesList = new ArrayList<>();
        //Obtenemos el token con de SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", null); // Obtiene el token, o null si no existe

        if (token != null) {
            // Aquí puedes usar el token para tus peticiones o validaciones
            Log.d("Token", "Token recuperado: " + token);
        } else {
            // Si el token no existe, puede que el usuario no haya iniciado sesión o el token haya expirado
            Log.d("Token", "No hay token guardado.");
        }


        ApiService apiService = ApiClient.getApiService();

        apiService.listarHoteles("Bearer "+token).enqueue(new Callback<List<String>>() {//el 1234 deve de ser edit.token para recojer el jwt
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> hotels = response.body();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            NewReservationActivity.this,
                            android.R.layout.simple_spinner_item,
                            hotels

                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerHotel.setAdapter(adapter);
                }

                System.out.println("Hotel seleccionado: ");
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e("API_ERROR", "Error al obtener hoteles: " + t.getMessage());
            }
        });

        // Aseguramos que el Spinner de hoteles esté configurado correctamente y que se maneje la selección del usuario
        spinnerHotel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos el hotel seleccionado del Spinner
                String selectedHotel = (String) parent.getItemAtPosition(position);

                // Validamos que el hotel seleccionado no sea nulo o vacío
                if (selectedHotel != null && !selectedHotel.isEmpty()) {
                    // Llamamos a la API para obtener las habitaciones del hotel seleccionado
                    apiService.listarHabitaciones("Bearer "+token ,selectedHotel).enqueue(new Callback<List<Map<String, Object>>>() {

                        @Override
                        public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                // Configuramos el Spinner de habitaciones con los datos obtenidos
                                habitacionesList = response.body();
                                List<String> roomDescriptions = new ArrayList<>();

                                for (Map<String, Object> habitacion : habitacionesList) {
                                    String numero = (String) habitacion.get("numero");
                                    String planta = (String) habitacion.get("planta");
                                    Double nmaxDouble = (Double) habitacion.get("nmax");
                                    int nmax = nmaxDouble != null ? nmaxDouble.intValue() : 1;
                                    if(nmax==0){
                                        nmax=2;
                                    }

                                    String description = String.format("Habitación %s - Planta %s (Max: %d personas)",
                                            numero, planta, nmax);
                                    roomDescriptions.add(description);
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        NewReservationActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        roomDescriptions
                                );
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerHabitacion.setAdapter(adapter);
                            } else {
                                Log.e("API_ERROR", "Respuesta vacía o no exitosa al obtener habitaciones");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                            // Registramos el error en caso de fallo en la llamada a la API
                            Log.e("API_ERROR", "Error al obtener habitaciones: " + t.getMessage());
                        }
                    });
                } else {
                    Log.e("SPINNER_ERROR", "El hotel seleccionado es nulo o vacío");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacemos nada si no se selecciona un hotel
            }
        });

        // Configurar el botón de crear reserva
        btnCreateReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearReserva();
            }
        });
    }

    private void crearReserva() {
        // Validar que todos los campos estén completos
        if (spinnerHotel.getSelectedItem() == null ||
                spinnerHabitacion.getSelectedItem() == null ||
                edtGuests.getText().toString().isEmpty()) {

            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Obtener el hotel seleccionado
            String hotelSeleccionado = spinnerHotel.getSelectedItem().toString();

            // Obtener el número de habitación seleccionada
            int posicionHabitacion = spinnerHabitacion.getSelectedItemPosition();
            String numeroHabitacion = (String) habitacionesList.get(posicionHabitacion).get("numero");

            // Obtener fechas de los DatePickers
            int diaInicio = datePickerCheckIn.getDayOfMonth();
            int mesInicio = datePickerCheckIn.getMonth();
            int anioInicio = datePickerCheckIn.getYear();
            String fechaInicio = String.format("%d-%02d-%02d", anioInicio, mesInicio + 1, diaInicio);//el mas uno es por que el mes esta malito y empieza desde 0 en vez de desde 1

            int diaFin = datePickerCheckOut.getDayOfMonth();
            int mesFin = datePickerCheckOut.getMonth();
            int anioFin = datePickerCheckOut.getYear();
            String fechaFin = String.format("%d-%02d-%02d", anioFin, mesFin + 1, diaFin);

            // Obtener cantidad de huéspedes
            int cantidadHuespedes = Integer.parseInt(edtGuests.getText().toString());

            // Obtener ID de usuario de SharedPreferences
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            int userId = preferences.getInt("USER_ID", -1);

            if (userId == -1) {
                Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Preparar los datos para enviar al servidor
            Map<String, Object> reservaData = new HashMap<>();
            // Convertimos el ID a String porque el backend espera un String
            reservaData.put("user_id", String.valueOf(userId));
            reservaData.put("hotel", hotelSeleccionado);
            reservaData.put("habitacion", numeroHabitacion);
            reservaData.put("fechaInicio", fechaInicio);
            reservaData.put("fechaFin", fechaFin);
            reservaData.put("cantidad", cantidadHuespedes);

            String token = preferences.getString("token", null);//esta duplicado , pero no se por que me obliga a ponerlo dos veces

            if (token != null) {
                // Aquí puedes usar el token para tus peticiones o validaciones
                Log.d("Token", "Token recuperado: " + token);
            } else {
                // Si el token no existe, puede que el usuario no haya iniciado sesión o el token haya expirado
                Log.d("Token", "No hay token guardado.");
            }

            ApiService apiService = ApiClient.getApiService();//esta duplicado , pero no se por que me obliga a ponerlo dos veces
            apiService.crearReserva("Bearer "+token,reservaData).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(NewReservationActivity.this, "Reserva creada con éxito", Toast.LENGTH_SHORT).show();
                        finish(); // Volver a la actividad anterior
                    } else {
                        // Intentamos obtener el mensaje de error del cuerpo de la respuesta
                        String errorMsg;
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg = response.message();
                        }

                        Toast.makeText(NewReservationActivity.this,
                                "Error al crear la reserva: " + errorMsg,
                                Toast.LENGTH_LONG).show();

                        Log.e("RESERVATION_ERROR", "Error respuesta: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(NewReservationActivity.this,
                            "Error de comunicación: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();

                    Log.e("RESERVATION_ERROR", "Error de comunicación: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("RESERVATION_ERROR", "Error al crear reserva: " + e.getMessage());
        }
    }
}