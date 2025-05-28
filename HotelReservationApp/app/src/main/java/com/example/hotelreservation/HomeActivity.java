package com.example.hotelreservation;

import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.example.hotelreservation.reservation.NewReservationActivity;
import com.example.hotelreservation.reservation.ReservationActivity;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private String userRole;
    private int hotelId;
    private String hotelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Obtener datos de la sesión
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userRole = preferences.getString("USER_ROLE", "user");
        hotelId = preferences.getInt("HOTEL_ID", -1);
        hotelName = preferences.getString("HOTEL_NAME", "");

        // Configurar vistas basadas en el rol
        setupViewsByRole();

        // Botón de Cerrar Sesión
        Button cerrarsesion = findViewById(R.id.btnLogout);
        cerrarsesion.setOnClickListener(v -> {
            // Cerrar sesión y volver a la pantalla de inicio de sesión
            finishAffinity(); // Cierra todas las actividades de la aplicación
            System.exit(0); // Finaliza el proceso de la aplicación
        });
    }

    private void setupViewsByRole() {
        // Elementos de UI comunes
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnNewReservation = findViewById(R.id.btnNewReservation);
        Button btnMyReservations = findViewById(R.id.btnMyReservations);
        
        // Elementos específicos por rol
        LinearLayout userPanel = findViewById(R.id.userPanel);
        LinearLayout adminPanel = findViewById(R.id.adminPanel);
        LinearLayout hotelManagerPanel = findViewById(R.id.hotelManagerPanel);
        TextView tvHotelName = findViewById(R.id.tvHotelName);
        
        // Configuración por rol
        switch (userRole) {
            case "admin":
                tvWelcome.setText("Bienvenido Administrador");
                adminPanel.setVisibility(View.VISIBLE);
                hotelManagerPanel.setVisibility(View.GONE);
                userPanel.setVisibility(View.GONE); // Ocultar panel de usuario para evitar superposición
                break;
                
            case "hotelmanager":
                tvWelcome.setText("Bienvenido Gerente de Hotel");
                tvHotelName.setText("Hotel: " + hotelName);
                tvHotelName.setVisibility(View.VISIBLE);
                adminPanel.setVisibility(View.GONE);
                hotelManagerPanel.setVisibility(View.VISIBLE);
                userPanel.setVisibility(View.GONE); // Ocultar panel de usuario
                btnMyReservations.setText("Ver Todas las Reservas");
                break;
                
            default: // usuario normal
                tvWelcome.setText("Bienvenido Usuario");
                adminPanel.setVisibility(View.GONE);
                hotelManagerPanel.setVisibility(View.GONE);
                userPanel.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void navigateToNewReservation(View view) {
        Intent intent = new Intent(this, NewReservationActivity.class);
        startActivity(intent);
    }

    public void navigateToMyReservations(View view) {
        Intent intent = new Intent(this, ReservationActivity.class);
        
        // Si es hotel manager, pasar el ID del hotel para mostrar todas las reservas
        if ("hotelmanager".equals(userRole) && hotelId > 0) {
            intent.putExtra("IS_HOTEL_MANAGER", true);
            intent.putExtra("HOTEL_ID", hotelId);
        }
        
        startActivity(intent);
    }
    
    public void navigateToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    public void navigateToRegisterHotelManager(View view) {
        Intent intent = new Intent(this, com.example.hotelreservation.admin.RegisterHotelManagerActivity.class);
        startActivity(intent);
    }    public void navigateToRoomManagement(View view) {
        Intent intent = new Intent(this, com.example.hotelreservation.room.RoomManagementActivity.class);
        // Pasar información sobre el rol para que la actividad sepa cómo cargar las habitaciones
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
    }
    
    public void navigateToAllReservations(View view) {
        Intent intent = new Intent(this, ReservationActivity.class);
        intent.putExtra("IS_ADMIN", true);
        startActivity(intent);
    }
    
    public void navigateToHotelManagement(View view) {
        Intent intent = new Intent(this, com.example.hotelreservation.admin.HotelManagementActivity.class);
        startActivity(intent);
    }
}