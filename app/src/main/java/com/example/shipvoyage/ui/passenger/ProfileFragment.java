package com.example.shipvoyage.ui.passenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.ui.auth.UserTypeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private TextView usernameField;
    private TextView emailField;
    private TextView phoneField;
    private EditText currentPasswordField;
    private EditText newPasswordField;
    private EditText confirmPasswordField;
    private TextView memberSinceValue;
    private TextView totalBookingsLabel;
    private Button logoutButton;
    private Button changePasswordToggleButton;
    private Button changePasswordButton;
    private LinearLayout changePasswordSection;
    private UserDAO userDAO;
    private BookingDAO bookingDAO;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userDAO = new UserDAO();
        bookingDAO = new BookingDAO();
        mAuth = FirebaseAuth.getInstance();
        
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please log in to view your profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), UserTypeActivity.class));
            requireActivity().finish();
            return;
        }
        
        initViews(view);
        loadUserProfile();
        loadBookingStats();
    }

    private void initViews(View view) {
        usernameField = view.findViewById(R.id.usernameField);
        emailField = view.findViewById(R.id.emailField);
        phoneField = view.findViewById(R.id.phoneField);
        currentPasswordField = view.findViewById(R.id.currentPasswordField);
        newPasswordField = view.findViewById(R.id.newPasswordField);
        confirmPasswordField = view.findViewById(R.id.confirmPasswordField);
        memberSinceValue = view.findViewById(R.id.memberSinceValue);
        totalBookingsLabel = view.findViewById(R.id.totalBookingsLabel);
        logoutButton = view.findViewById(R.id.logoutButton);
        changePasswordToggleButton = view.findViewById(R.id.changePasswordToggleButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        changePasswordSection = view.findViewById(R.id.changePasswordSection);
        
        logoutButton.setOnClickListener(v -> logout());
        changePasswordToggleButton.setOnClickListener(v -> toggleChangePasswordSection());
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void toggleChangePasswordSection() {
        if (changePasswordSection.getVisibility() == View.GONE) {
            changePasswordSection.setVisibility(View.VISIBLE);
            changePasswordToggleButton.setText("Hide Change Password");
        } else {
            changePasswordSection.setVisibility(View.GONE);
            changePasswordToggleButton.setText("Change Password");
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        }
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        
        currentUserId = mAuth.getCurrentUser().getUid();
        userDAO.getUser(currentUserId).addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
                phoneField.setText(user.getPhone() != null ? user.getPhone() : "Not provided");
                
                if (user.getCreatedAt() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    memberSinceValue.setText(sdf.format(new Date(user.getCreatedAt())));
                } else {
                    memberSinceValue.setText("N/A");
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBookingStats() {
        if (mAuth.getCurrentUser() == null) {
            totalBookingsLabel.setText("0");
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        bookingDAO.getAllBookings().addOnSuccessListener(snapshot -> {
            long count = 0;
            for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                com.example.shipvoyage.model.Booking booking = bookingSnapshot.getValue(com.example.shipvoyage.model.Booking.class);
                if (booking != null && userId.equals(booking.getUserId())) {
                    count++;
                }
            }
            totalBookingsLabel.setText(String.valueOf(count));
        }).addOnFailureListener(e -> {
            totalBookingsLabel.setText("0");
        });
    }

    private void changePassword() {
        String currentPassword = currentPasswordField.getText().toString().trim();
        String newPassword = newPasswordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all password fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (newPassword.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please log in to change password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String email = mAuth.getCurrentUser().getEmail();
        mAuth.signInWithEmailAndPassword(email, currentPassword)
            .addOnSuccessListener(authResult -> {
                mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to change password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
            });
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(requireActivity(), UserTypeActivity.class));
        requireActivity().finish();
    }
}
