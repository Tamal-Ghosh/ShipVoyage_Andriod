package com.example.shipvoyage.util;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import com.example.shipvoyage.R;
import com.example.shipvoyage.ui.admin.AdminDashboardActivity;
import com.example.shipvoyage.ui.admin.CustomerListActivity;
import com.example.shipvoyage.ui.admin.ManageRoomsActivity;
import com.example.shipvoyage.ui.admin.ManageShipsActivity;
import com.example.shipvoyage.ui.admin.ManageTourInstancesActivity;
import com.example.shipvoyage.ui.admin.ManageToursActivity;
import com.example.shipvoyage.ui.admin.ViewBookingsActivity;

public class AdminNavHelper {

    public static void setupNavigationMenu(Activity activity, View anchorView) {
        PopupMenu popupMenu = new PopupMenu(activity, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.admin_nav_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_dashboard) {
                    intent = new Intent(activity, AdminDashboardActivity.class);
                } else if (id == R.id.nav_ships) {
                    intent = new Intent(activity, ManageShipsActivity.class);
                } else if (id == R.id.nav_tours) {
                    intent = new Intent(activity, ManageToursActivity.class);
                } else if (id == R.id.nav_instances) {
                    intent = new Intent(activity, ManageTourInstancesActivity.class);
                } else if (id == R.id.nav_rooms) {
                    intent = new Intent(activity, ManageRoomsActivity.class);
                } else if (id == R.id.nav_bookings) {
                    intent = new Intent(activity, ViewBookingsActivity.class);
                } else if (id == R.id.nav_customers) {
                    intent = new Intent(activity, CustomerListActivity.class);
                }

                if (intent != null) {
                    activity.startActivity(intent);
                }
                return true;
            }
        });

        popupMenu.show();
    }
}
