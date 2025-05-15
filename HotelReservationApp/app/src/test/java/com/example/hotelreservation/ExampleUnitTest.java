package com.example.hotelreservation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelreservation.API.ApiClient;
import com.example.hotelreservation.API.ApiService;
import com.example.hotelreservation.API.LoginRequest;
import com.example.hotelreservation.API.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private TextView txtErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ApiClient with the application context
        ApiClient.init(getApplicationContext());

        edtUsername = findViewById(R.id.username);
        edtPassword = findViewById(R.id.password);
        txtErrorMessage = findViewById(R.id.errorMessage);

        // Check if there is an existing session
        checkExistingSession();
    }

    private void checkExistingSession() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = preferences.getString("TOKEN", null);
        if (token != null) {
            // If a token exists, navigate to the main app screen
            navigateToHomeScreen();
        }
    }

    public void btnLoginClick(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            txtErrorMessage.setText("Please enter both username and password.");
            return;
        }

        // Create a login request object
        LoginRequest loginRequest = new LoginRequest(username, password);
        ApiService apiService = ApiClient.getApiService();

        // Make the login API call
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        // Save the token and navigate to the home screen
                        saveUserSession(loginResponse);
                        navigateToHomeScreen();
                    } else {
                        txtErrorMessage.setText(loginResponse.getMessage());
                    }
                } else {
                    txtErrorMessage.setText("Login failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginError", t.getMessage());
                txtErrorMessage.setText("An error occurred. Please try again.");
            }
        });
    }

    private void saveUserSession(LoginResponse loginResponse) {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("TOKEN", loginResponse.getToken());
        editor.putString("USER_ROLE", loginResponse.getRol());
        editor.putInt("USER_ID", loginResponse.getUserId());
        editor.apply();
    }

    private void navigateToHomeScreen() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close the login activity
    }
}