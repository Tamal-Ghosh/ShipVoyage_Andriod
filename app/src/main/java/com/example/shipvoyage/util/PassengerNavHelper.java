package com.example.shipvoyage.util;
import android.app.Activity;
import android.content.Intent;
import com.example.shipvoyage.R;
import com.example.shipvoyage.ui.passenger.MyBookingsActivity;
import com.example.shipvoyage.ui.passenger.PassengerHomeActivity;
import com.example.shipvoyage.ui.passenger.ProfileActivity;
import com.example.shipvoyage.ui.passenger.SupportActivity;
import com.example.shipvoyage.ui.passenger.UpcomingToursActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
public class PassengerNavHelper {
    public static void setupBottomNavigation(Activity activity, BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.nav_home) {
                intent = new Intent(activity, PassengerHomeActivity.class);
            } else if (id == R.id.nav_my_bookings) {
                intent = new Intent(activity, MyBookingsActivity.class);
            } else if (id == R.id.nav_upcoming_tours) {
                intent = new Intent(activity, UpcomingToursActivity.class);
            } else if (id == R.id.nav_support) {
                intent = new Intent(activity, SupportActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(activity, ProfileActivity.class);
            }
            if (intent != null) {
                activity.startActivity(intent);
                return true;
            }
            return false;
        });
    }
    public static void setupNavigationMenu(Activity activity, android.view.View anchorView) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(activity, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.passenger_nav_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;
                if (id == R.id.nav_home) {
                    intent = new Intent(activity, PassengerHomeActivity.class);
                } else if (id == R.id.nav_my_bookings) {
                    intent = new Intent(activity, MyBookingsActivity.class);
                } else if (id == R.id.nav_upcoming_tours) {
                    intent = new Intent(activity, UpcomingToursActivity.class);
                } else if (id == R.id.nav_support) {
                    intent = new Intent(activity, SupportActivity.class);
                } else if (id == R.id.nav_profile) {
                    intent = new Intent(activity, ProfileActivity.class);
                }
                if (intent != null) {
                    activity.startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}