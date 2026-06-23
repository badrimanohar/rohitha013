package com.example.agriguard.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agriguard.databinding.ActivitySignupBinding;
import com.example.agriguard.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.agriguard.R;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isPasswordStrong = false;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        setupGoogleSignIn();
        setupPasswordValidation();

        binding.btnSignup.setOnClickListener(v -> handleSignUp());
        binding.btnGoogleSignup.setOnClickListener(v -> googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent()));
        binding.tvGoToLogin.setOnClickListener(v -> finish());
        
        // Initial state
        binding.btnSignup.setEnabled(false);
    }

    private void setupPasswordValidation() {
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        TextWatcher commonWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etName.addTextChangedListener(commonWatcher);
        binding.etEmail.addTextChangedListener(commonWatcher);
        binding.etConfirmPassword.addTextChangedListener(commonWatcher);
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            binding.llPasswordStrength.setVisibility(View.GONE);
            isPasswordStrong = false;
            return;
        }

        binding.llPasswordStrength.setVisibility(View.VISIBLE);
        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[@#$%^&+=!].*")) strength++;

        int color;
        String label;
        int progress = (strength * 100) / 5;

        if (strength <= 2) {
            color = Color.RED;
            label = "Weak";
            isPasswordStrong = false;
        } else if (strength <= 4) {
            color = Color.parseColor("#FFA500"); // Orange
            label = "Medium";
            isPasswordStrong = false;
        } else {
            color = Color.parseColor("#2E7D32"); // Dark Green
            label = "Strong";
            isPasswordStrong = true;
        }

        binding.pbPasswordStrength.setProgress(progress);
        binding.pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(color));
        binding.tvPasswordStrength.setText("Strength: " + label);
        binding.tvPasswordStrength.setTextColor(color);
        
        validateForm();
    }

    private void validateForm() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        boolean isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean passwordsMatch = password.equals(confirmPassword) && !confirmPassword.isEmpty();

        binding.btnSignup.setEnabled(!name.isEmpty() && isEmailValid && isPasswordStrong && passwordsMatch);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void handleSignUp() {
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        if (!isPasswordStrong) {
            binding.tilPassword.setError("Password does not meet requirements");
            return;
        }

        binding.btnSignup.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToDatabase(firebaseUser.getUid(), name, email);
                        }
                    } else {
                        binding.btnSignup.setEnabled(true);
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(SignUpActivity.this, "❌ " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String email) {
        User user = new User(name, email, null, "email", System.currentTimeMillis());
        mDatabase.child("users").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mAuth.signOut(); // Sign out after registration
                        Toast.makeText(SignUpActivity.this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        finishAffinity();
                    } else {
                        binding.btnSignup.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserInDatabase(user);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Google authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInDatabase(FirebaseUser firebaseUser) {
        mDatabase.child("users").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    User newUser = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(),
                            firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                            "google", System.currentTimeMillis());
                    mDatabase.child("users").child(firebaseUser.getUid()).setValue(newUser);
                }
                startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                finishAffinity();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
