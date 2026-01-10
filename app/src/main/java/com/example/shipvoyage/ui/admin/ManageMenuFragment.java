package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.shipvoyage.R;

public class ManageMenuFragment extends Fragment {

    public ManageMenuFragment() {
        super(R.layout.fragment_manage_menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);

        Button manageShipsBtn = view.findViewById(R.id.manageShipsBtn);
        manageShipsBtn.setOnClickListener(v -> navController.navigate(R.id.manageShipsFragment));

        Button manageRoomsBtn = view.findViewById(R.id.manageRoomsBtn);
        manageRoomsBtn.setOnClickListener(v -> navController.navigate(R.id.manageRoomsFragment));

        Button manageToursBtn = view.findViewById(R.id.manageToursBtn);
        manageToursBtn.setOnClickListener(v -> navController.navigate(R.id.manageToursFragment));

        Button manageTourInstancesBtn = view.findViewById(R.id.manageTourInstancesBtn);
        manageTourInstancesBtn.setOnClickListener(v -> navController.navigate(R.id.manageTourInstancesFragment));

        Button manageFeaturedPhotosBtn = view.findViewById(R.id.manageFeaturedPhotosBtn);
        manageFeaturedPhotosBtn.setOnClickListener(v -> navController.navigate(R.id.manageFeaturedPhotosFragment));
    }
}
