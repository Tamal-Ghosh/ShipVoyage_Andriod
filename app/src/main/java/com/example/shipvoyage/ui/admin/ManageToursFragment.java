package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.TourAdapter;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public class ManageToursFragment extends Fragment {
    private RecyclerView toursRecyclerView;
    private EditText nameField, fromField, toField, descriptionField, searchField;
    private Button saveBtn, cancelBtn, searchBtn, addToggleBtn;
    private View formContainer;
    private TourDAO tourDAO;
    private List<Tour> toursList = new ArrayList<>();
    private TourAdapter tourAdapter;
    private String editingTourId = null;
    private boolean isFormVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_tours, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tourDAO = new TourDAO();
        initViews(view);
        setupListeners();
        loadTours();
    }

    private void initViews(View view) {
        toursRecyclerView = view.findViewById(R.id.toursRecyclerView);
        nameField = view.findViewById(R.id.nameField);
        fromField = view.findViewById(R.id.fromField);
        toField = view.findViewById(R.id.toField);
        descriptionField = view.findViewById(R.id.descriptionField);
        searchField = view.findViewById(R.id.searchField);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        addToggleBtn = view.findViewById(R.id.addToggleBtn);
        formContainer = view.findViewById(R.id.formContainer);

        toursRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tourAdapter = new TourAdapter(new TourAdapter.OnTourClickListener() {
            @Override
            public void onEditClick(Tour tour) {
                editingTourId = tour.getId();
                nameField.setText(tour.getName());
                fromField.setText(tour.getFrom());
                toField.setText(tour.getTo());
                descriptionField.setText(tour.getDescription());
                toggleForm(true);
            }

            @Override
            public void onDeleteClick(Tour tour) {
                tourDAO.deleteTour(tour.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Tour deleted", Toast.LENGTH_SHORT).show();
                    loadTours();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete tour", Toast.LENGTH_SHORT).show();
                });
            }
        });
        toursRecyclerView.setAdapter(tourAdapter);
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveTour());
        cancelBtn.setOnClickListener(v -> clearForm());
        searchBtn.setOnClickListener(v -> performSearch());
        addToggleBtn.setOnClickListener(v -> {
            if (isFormVisible) {
                clearForm();
                toggleForm(false);
            } else {
                toggleForm(true);
            }
        });
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadTours() {
        tourDAO.getAllTours().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Tour> newTours = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    if (tour != null) {
                        newTours.add(tour);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toursList.clear();
                        toursList.addAll(newTours);
                        updateRecyclerView();
                    });
                }
            });
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load tours", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveTour() {
        String name = nameField.getText().toString().trim();
        String from = fromField.getText().toString().trim();
        String to = toField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();

        if (name.isEmpty() || from.isEmpty() || to.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Tour existingTour : toursList) {
            if (existingTour.getName().equalsIgnoreCase(name) &&
                (editingTourId == null || !existingTour.getId().equals(editingTourId))) {
                Toast.makeText(requireContext(), "Tour name already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String tourId = editingTourId != null ? editingTourId : tourDAO.toursRef.push().getKey();
        if (tourId != null) {
            Tour tour = new Tour(tourId, name, from, to, description);
            tourDAO.addTour(tour).addOnSuccessListener(unused -> {
                Toast.makeText(requireContext(), editingTourId != null ? "Tour updated successfully" : "Tour saved successfully", Toast.LENGTH_SHORT).show();
                clearForm();
                toggleForm(false);
                loadTours();
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to save tour", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void clearForm() {
        editingTourId = null;
        nameField.setText("");
        fromField.setText("");
        toField.setText("");
        descriptionField.setText("");
        toggleForm(false);
    }

    private void toggleForm(boolean show) {
        isFormVisible = show;
        formContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        addToggleBtn.setText(show ? "Close Form" : "Add Tour");
        if (show) {
            nameField.requestFocus();
        }
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            tourAdapter.submitList(new ArrayList<>(toursList));
            return;
        }

        List<Tour> filteredTours = new ArrayList<>();
        for (Tour tour : toursList) {
            if (tour.getName().toLowerCase().contains(query) ||
                tour.getFrom().toLowerCase().contains(query) ||
                tour.getTo().toLowerCase().contains(query)) {
                filteredTours.add(tour);
            }
        }
        tourAdapter.submitList(new ArrayList<>(filteredTours));
    }

    private void updateRecyclerView() {
        tourAdapter.submitList(new ArrayList<>(toursList));
    }
}
