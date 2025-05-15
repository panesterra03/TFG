package com.example.hotelreservation;

import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import com.example.hotelreservation.reservation.NewReservationActivity;
import com.example.hotelreservation.reservation.ReservationActivity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Botón de Cerrar Sesión
        Button cerrarsesion = findViewById(R.id.btnLogout);
        cerrarsesion.setOnClickListener(v -> {
            // Cerrar sesión y volver a la pantalla de inicio de sesión
            finishAffinity(); // Cierra todas las actividades de la aplicación
            System.exit(0); // Finaliza el proceso de la aplicación
        });
    }
    // protected void onCreate(Bundle savedInstanceState) {
    //     super.onCreate(savedInstanceState);
    //     setContentView(R.layout.activity_reservation);
    
    //     // Botón de Nueva Reserva
    //     Button btnMakeReservation = findViewById(R.id.btnMakeReservation);
    //     btnMakeReservation.setOnClickListener(v -> {//poner el menu donde reservar


    //     });
    
    //     // Botón Flotante de Nueva Reserva
    //     FloatingActionButton fabNewReservation = findViewById(R.id.fabNewReservation);
    //     fabNewReservation.setOnClickListener(v -> {//guarda lña nueva reserva
   
    //     });
    // }
    public void navigateToNewReservation(View view) {
        Intent intent = new Intent(this, NewReservationActivity.class);
        startActivity(intent);
    }

    public void navigateToMyReservations(View view) {
        Intent intent = new Intent(this, ReservationActivity.class);
        startActivity(intent);
    }
}