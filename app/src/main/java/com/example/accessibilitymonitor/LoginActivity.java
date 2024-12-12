package com.example.accessibilitymonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerRedirectButton;
    private AuthDatabaseHelper dbHelper;
    private TextInputLayout emailInputLayout, passwordInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerRedirectButton = findViewById(R.id.registerRedirectButton);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        dbHelper = new AuthDatabaseHelper(this);

        // Adding TextWatcher for email input
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not needed for this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > 0) {
                    // Hide the hint when text is present
                    emailInputLayout.setHintEnabled(false);
                } else {
                    // Show the hint when no text is present
                    emailInputLayout.setHintEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


        });

        // Adding TextWatcher for password input
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not needed for this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > 0) {
                    // Hide the hint when text is present
                    passwordInputLayout.setHintEnabled(false);
                } else {
                    // Show the hint when no text is present
                    passwordInputLayout.setHintEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this case
            }
        });


        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = dbHelper.authenticateAndGenerateToken(email, password);
            if (token != null) {
                SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                prefs.edit().putString("auth_token", token).apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        registerRedirectButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
