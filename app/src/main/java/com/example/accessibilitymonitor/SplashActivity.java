package com.example.accessibilitymonitor;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private AuthDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        dbHelper = new AuthDatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        if (token != null && !dbHelper.isTokenExpired(token)) {
            navigateToHome();
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Remove any saved token
            editor.apply();
            navigateToLogin();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close SplashActivity
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close SplashActivity
    }
}
