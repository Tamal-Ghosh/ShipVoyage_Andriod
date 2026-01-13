package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.CustomerAdapter;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.User;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerListFragment extends Fragment {
    private RecyclerView customersRecyclerView;
    private Spinner instanceSpinner;
    private EditText searchField;
    private Button searchBtn;
    private UserDAO userDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private BookingDAO bookingDAO;
    private List<User> customersList = new ArrayList<>();
    private List<Tour> toursList = new ArrayList<>();
    private List<TourInstance> instancesList = new ArrayList<>();
    private CustomerAdapter customerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userDAO = new UserDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        bookingDAO = new BookingDAO();
        
        initViews(view);
        setupListeners();
        loadTours();
        loadCustomers();
    }

    private void initViews(View view) {
        customersRecyclerView = view.findViewById(R.id.customersRecyclerView);
        instanceSpinner = view.findViewById(R.id.instanceSpinner);
        searchField = view.findViewById(R.id.searchField);
        searchBtn = view.findViewById(R.id.searchBtn);

        customersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        customerAdapter = new CustomerAdapter(new CustomerAdapter.OnCustomerClickListener() {
            @Override
            public void onViewClick(User customer) {
                showEditCustomerDialog(customer);
            }

            @Override
            public void onDeleteClick(User customer) {
                userDAO.deleteUser(customer.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Customer deleted", Toast.LENGTH_SHORT).show();
                    loadCustomers();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete customer", Toast.LENGTH_SHORT).show();
                });
            }
        });
        customersRecyclerView.setAdapter(customerAdapter);
    }

    private void setupListeners() {
        searchBtn.setOnClickListener(v -> performSearch());
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

        // Filter when tour instance selection changes
        instanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                performSearch();
            }
        });
    }

    private void loadTours() {
        tourDAO.getAllTours().addOnSuccessListener(dataSnapshot -> {
            toursList.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Tour tour = snapshot.getValue(Tour.class);
                if (tour != null) {
                    toursList.add(tour);
                }
            }
            loadInstances();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load tours", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            instancesList.clear();
            List<String> instanceNames = new ArrayList<>();
            instanceNames.add("Select Tour Instance");
            
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                TourInstance instance = snapshot.getValue(TourInstance.class);
                if (instance != null) {
                    for (Tour tour : toursList) {
                        if (tour.getId().equals(instance.getTourId())) {
                            instance.setTourName(tour.getName());
                            break;
                        }
                    }
                    instancesList.add(instance);
                    instanceNames.add(instance.getTourName() + " - " + instance.getStartDate());
                }
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, instanceNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            instanceSpinner.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load instances", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCustomers() {
        userDAO.getAllUsers().addOnSuccessListener(dataSnapshot -> {
            customersList.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                User user = snapshot.getValue(User.class);
                if (user != null && "PASSENGER".equalsIgnoreCase(user.getRole())) {
                    customersList.add(user);
                }
            }
            // Apply current filters (search text + instance selection)
            performSearch();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load customers", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateRecyclerView() {
        customerAdapter.submitList(new ArrayList<>(customersList));
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        int selectedPosition = instanceSpinner.getSelectedItemPosition();

        boolean hasValidInstanceSelection = selectedPosition > 0 && (selectedPosition - 1) < instancesList.size();

        if (!hasValidInstanceSelection) {
            // No instance selected: filter only by search
            List<User> filteredList = new ArrayList<>();
            for (User customer : customersList) {
                boolean matchesSearch = query.isEmpty() ||
                        (customer.getName() != null && customer.getName().toLowerCase().contains(query)) ||
                        (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(query)) ||
                        (customer.getPhone() != null && customer.getPhone().contains(query));
                if (matchesSearch) {
                    filteredList.add(customer);
                }
            }
            customerAdapter.submitList(filteredList);
            return;
        }

        // Instance selected: fetch bookings and filter customers by those who booked the instance
        String selectedInstanceId = instancesList.get(selectedPosition - 1).getId();
        bookingDAO.getAllBookings().addOnSuccessListener(snapshot -> {
            java.util.HashSet<String> userIdsForInstance = new java.util.HashSet<>();
            for (com.google.firebase.database.DataSnapshot bookingSnap : snapshot.getChildren()) {
                com.example.shipvoyage.model.Booking booking = bookingSnap.getValue(com.example.shipvoyage.model.Booking.class);
                if (booking != null && selectedInstanceId.equals(booking.getTourInstanceId())) {
                    // Optionally skip cancelled bookings
                    // if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) continue;
                    if (booking.getUserId() != null) {
                        userIdsForInstance.add(booking.getUserId());
                    }
                }
            }

            List<User> filteredList = new ArrayList<>();
            for (User customer : customersList) {
                boolean matchesSearch = query.isEmpty() ||
                        (customer.getName() != null && customer.getName().toLowerCase().contains(query)) ||
                        (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(query)) ||
                        (customer.getPhone() != null && customer.getPhone().contains(query));
                boolean bookedThisInstance = customer.getId() != null && userIdsForInstance.contains(customer.getId());
                if (matchesSearch && bookedThisInstance) {
                    filteredList.add(customer);
                }
            }
            customerAdapter.submitList(filteredList);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to filter by instance", Toast.LENGTH_SHORT).show();
        });
    }

    private void showEditCustomerDialog(User customer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Customer");
        
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Name");
        nameInput.setText(customer.getName());
        layout.addView(nameInput);
        
        final EditText emailInput = new EditText(requireContext());
        emailInput.setHint("Email");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setText(customer.getEmail());
        layout.addView(emailInput);
        
        final EditText phoneInput = new EditText(requireContext());
        phoneInput.setHint("Phone");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneInput.setText(customer.getPhone());
        layout.addView(phoneInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            customer.setName(name);
            customer.setEmail(email);
            customer.setPhone(phone);
            
            userDAO.updateUser(customer).addOnSuccessListener(unused -> {
                Toast.makeText(requireContext(), "Customer updated successfully", Toast.LENGTH_SHORT).show();
                loadCustomers();
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to update customer", Toast.LENGTH_SHORT).show();
            });
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        builder.create().show();
    }
}
