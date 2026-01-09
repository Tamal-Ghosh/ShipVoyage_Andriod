package com.example.shipvoyage;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.shipvoyage.ui.admin.AdminDashboardActivity;
import com.example.shipvoyage.ui.admin.ManageShipsActivity;
import com.example.shipvoyage.ui.admin.ManageTourInstancesActivity;
import com.example.shipvoyage.ui.admin.ManageToursActivity;
import com.example.shipvoyage.ui.passenger.PassengerHomeActivity;
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
            Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });
        Button manageShipsButton = findViewById(R.id.manageShipsButton);
        manageShipsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageShipsActivity.class);
            startActivity(intent);
        });
        Button manageToursButton = findViewById(R.id.manageToursButton);
        manageToursButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageToursActivity.class);
            startActivity(intent);
        });
        Button manageInstancesButton = findViewById(R.id.manageInstancesButton);
        manageInstancesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageTourInstancesActivity.class);
            startActivity(intent);
        });
        Button passengerHomeButton = findViewById(R.id.passengerHomeButton);
        passengerHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PassengerHomeActivity.class);
            startActivity(intent);
        });
    }
}