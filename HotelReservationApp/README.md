### New MainActivity for Hotel Reservation Software

```java
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

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);

        // Check if there is an existing session
        checkExistingSession();
    }

    private void checkExistingSession() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (preferences.contains("USER_ID")) {
            // User is already logged in, redirect to the main menu
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void btnLoginClick(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            txtErrorMessage.setText("Please enter both username and password.");
            return;
        }

        // Create a login request
        LoginRequest loginRequest = new LoginRequest(username, password);
        ApiService apiService = ApiClient.getApiService();
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        // Save user details in SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("USER_ID", loginResponse.getUserId());
                        editor.putString("USER_ROLE", loginResponse.getRol());
                        editor.apply();

                        // Redirect to the main menu
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        startActivity(intent);
                        finish();
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
}
```

### Layout XML (activity_main.xml)

You will also need a layout file for this activity. Below is a simple example of what the layout might look like:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <EditText
        android:id="@+id/edtUsername"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Username" />

    <EditText
        android:id="@+id/edtPassword"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:inputType="textPassword" />

    <TextView
        android:id="@+id/txtErrorMessage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/holo_red_dark"
        android:layout_marginTop="8dp" />

    <Button
        android:id="@+id/btnLogin"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login"
        android:onClick="btnLoginClick" />

</LinearLayout>
```

### Notes
- Ensure that you have the necessary API service methods defined in your `ApiService` interface for handling the login request.
- The `MenuActivity` should be created to handle the main menu after a successful login.
- Adjust the package names and imports according to your project structure.
- Make sure to handle any additional requirements such as input validation, error handling, and user feedback as needed.