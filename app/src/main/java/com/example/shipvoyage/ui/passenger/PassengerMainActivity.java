package com.example.shipvoyage.ui.passenger;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.shipvoyage.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PassengerMainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_passenger_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.passengerMainRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupNavigation();
        setupBrandBar();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.passenger_nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        
        bottomNav = findViewById(R.id.passengerBottomNav);
        
        if (navController == null) {
            return;
        }
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                navController.navigate(R.id.passengerHomeFragment);
                return true;
            } else if (itemId == R.id.nav_upcoming_tours) {
                navController.navigate(R.id.upcomingToursFragment);
                return true;
            } else if (itemId == R.id.nav_my_bookings) {
                navController.navigate(R.id.myBookingsFragment);
                return true;
            } else if (itemId == R.id.nav_support) {
                navController.navigate(R.id.supportFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                navController.navigate(R.id.profileFragment);
                return true;
            }
            
            return false;
        });
    }

    private void setupBrandBar() {
        findViewById(R.id.passengerBrandBar).setOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.passengerHomeFragment);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (!navController.popBackStack()) {
            finishAffinity();
        }
    }
}
