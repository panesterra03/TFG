package com.example.hotelreservation.utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelreservation.MainActivity;
import com.example.hotelreservation.R;

public class AuthDiagnosticActivity extends AppCompatActivity {
    
    private TextView tvDiagnosticResult;
    private Button btnRunDiagnostic, btnTestToken, btnBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_diagnostic);
        
        // Initialize UI components
        tvDiagnosticResult = findViewById(R.id.tvDiagnosticResult);
        btnRunDiagnostic = findViewById(R.id.btnRunDiagnostic);
        btnTestToken = findViewById(R.id.btnTestToken);
        btnBack = findViewById(R.id.btnBack);
        
        // Set up button click listeners
        btnRunDiagnostic.setOnClickListener(v -> {
            tvDiagnosticResult.setText("Ejecutando diagnóstico...");
            String result = AuthDiagnostic.runDiagnostics(this, tvDiagnosticResult);
        });
        
        btnTestToken.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String token = preferences.getString("token", "");
            if (token.isEmpty()) {
                tvDiagnosticResult.setText("No hay token para probar");
                return;
            }
            AuthDiagnostic.testTokenWithApi(this, token, tvDiagnosticResult);
        });
        
        btnBack.setOnClickListener(v -> finish());
        
        // Run initial diagnostics
        String result = AuthDiagnostic.runDiagnostics(this, tvDiagnosticResult);
    }
}
