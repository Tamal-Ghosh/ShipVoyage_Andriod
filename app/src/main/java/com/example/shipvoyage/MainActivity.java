package com.example.shipvoyage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shipvoyage.ui.admin.AdminMainActivity;
import com.example.shipvoyage.ui.passenger.PassengerMainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        Button adminDashboardButton = findViewById(R.id.adminDashboardButton);
        adminDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
            startActivity(intent);
        });

        Button manageButton = findViewById(R.id.manageButton);
        LinearLayout manageSubmenu = findViewById(R.id.manageSubmenu);
        manageButton.setOnClickListener(v -> {
            if (manageSubmenu.getVisibility() == android.view.View.GONE) {
                manageSubmenu.setVisibility(android.view.View.VISIBLE);
                manageButton.setText("Manage \u25b2");
            } else {
                manageSubmenu.setVisibility(android.view.View.GONE);
                manageButton.setText("Manage \u25bc");
            }
        });

        Button passengerHomeButton = findViewById(R.id.passengerHomeButton);
        passengerHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PassengerMainActivity.class);
            startActivity(intent);
        });
    }
}