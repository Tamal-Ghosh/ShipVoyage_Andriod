package com.example.shipvoyage.ui.admin;

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

public class AdminMainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminMainRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupNavigation();
        setupBrandBar();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.admin_nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        
        bottomNav = findViewById(R.id.adminBottomNav);
        
        if (navController == null) {
            return;
        }
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_dashboard) {
                navController.navigate(R.id.adminDashboardFragment);
                return true;
            } else if (itemId == R.id.nav_manage) {
                navController.navigate(R.id.manageMenuFragment);
                return true;
            } else if (itemId == R.id.nav_bookings) {
                navController.navigate(R.id.viewBookingsFragment);
                return true;
            } else if (itemId == R.id.nav_customers) {
                navController.navigate(R.id.customerListFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                navController.navigate(R.id.adminProfileFragment);
                return true;
            }
            
            return false;
        });
    }

    private void setupBrandBar() {
        findViewById(R.id.adminBrandBar).setOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.adminDashboardFragment);
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
