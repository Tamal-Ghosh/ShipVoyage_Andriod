package com.example.shipvoyage.ui.admin;

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
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.ui.auth.UserTypeActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class AdminProfileFragment extends Fragment {
    private EditText emailField, phoneField, nameField;
    private EditText currentPasswordField, newPasswordField, confirmPasswordField;
    private Button logoutButton, changePasswordButton, changePasswordToggleButton, editButton;
    private LinearLayout changePasswordSection;
    private UserDAO userDAO;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private boolean isEditing = false;
    private User userBackup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userDAO = new UserDAO();
        mAuth = FirebaseAuth.getInstance();
        
        initViews(view);
        loadUserProfile();
    }

    private void initViews(View view) {
        emailField = view.findViewById(R.id.emailField);
        phoneField = view.findViewById(R.id.phoneField);
        nameField = view.findViewById(R.id.nameField);
        currentPasswordField = view.findViewById(R.id.currentPasswordField);
        newPasswordField = view.findViewById(R.id.newPasswordField);
        confirmPasswordField = view.findViewById(R.id.confirmPasswordField);
        logoutButton = view.findViewById(R.id.logoutButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        changePasswordToggleButton = view.findViewById(R.id.changePasswordToggleButton);
        editButton = view.findViewById(R.id.editButton);
        changePasswordSection = view.findViewById(R.id.changePasswordSection);

        logoutButton.setOnClickListener(v -> logout());
        changePasswordButton.setOnClickListener(v -> changePassword());
        changePasswordToggleButton.setOnClickListener(v -> toggleChangePasswordSection());
        editButton.setOnClickListener(v -> toggleEditMode());
    }

    private void toggleEditMode() {
        if (!isEditing) {
            isEditing = true;
            userBackup = new User();
            userBackup.setName(nameField.getText().toString());
            userBackup.setEmail(emailField.getText().toString());
            userBackup.setPhone(phoneField.getText().toString());
            
            nameField.setEnabled(true);
            emailField.setEnabled(true);
            phoneField.setEnabled(true);
            editButton.setText("Save");
            editButton.setTag("save");
        } else {
            saveUserProfile();
        }
    }

    private void saveUserProfile() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Name and email are required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        
        userDAO.usersRef.child(currentUserId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                isEditing = false;
                nameField.setEnabled(false);
                emailField.setEnabled(false);
                phoneField.setEnabled(false);
                editButton.setText("Edit Profile");
                editButton.setTag("edit");
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
            Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        userDAO.getUser(currentUserId).addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                emailField.setText(user.getEmail());
                phoneField.setText(user.getPhone() != null ? user.getPhone() : "Not provided");
                nameField.setText(user.getName() != null ? user.getName() : "Not provided");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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