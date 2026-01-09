package com.example.shipvoyage.ui.auth;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shipvoyage.R;

public class UserTypeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);

        Button adminLogin = findViewById(R.id.adminLogin);
        Button passengerLogin = findViewById(R.id.passengerLogin);

        adminLogin.setOnClickListener(v -> openLogin("admin"));
        passengerLogin.setOnClickListener(v -> openLogin("passenger"));
    }

    private void openLogin(String role) {
        var intent = new android.content.Intent(this, LoginActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}
