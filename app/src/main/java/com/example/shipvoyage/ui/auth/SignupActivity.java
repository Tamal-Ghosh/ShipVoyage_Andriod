package com.example.shipvoyage.ui.auth;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.User;
import com.google.firebase.auth.FirebaseAuth;
public class SignupActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText emailField;
    private EditText phoneField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private Button signupBtn;
    private TextView loginLink;
    private String selectedRole = "passenger";
    private UserDAO userDAO;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        selectedRole = getIntent().getStringExtra("role") != null ? getIntent().getStringExtra("role") : "passenger";
        userDAO = new UserDAO();
        mAuth = FirebaseAuth.getInstance();
        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        signupBtn = findViewById(R.id.signupBtn);
        loginLink = findViewById(R.id.loginLink);
        signupBtn.setOnClickListener(v -> attemptSignup());
        loginLink.setOnClickListener(v -> finish());
    }
    private void attemptSignup() {
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String confirm = confirmPasswordField.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Username, email, and password are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();
                User user = new User(uid, username, email, phone, selectedRole);
                userDAO.addUser(user).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> {
                String message = "Signup failed";
                if (e.getMessage().contains("email-already-in-use")) {
                    message = "Email already in use";
                } else if (e.getMessage().contains("weak-password")) {
                    message = "Password is too weak";
                }
                Toast.makeText(this, message + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}