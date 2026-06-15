package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agrinova.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private PrefsManager prefs;

    private static final String TAG = "AgriNovaLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Firebase
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

            // Shared Preferences
            prefs = new PrefsManager(this);

            // Setup
            setupLoginActions();

            // Animations
            applyAnimations();

        } catch (Exception e) {
            Log.e(TAG, "Initialization failed", e);
            Toast.makeText(this, "Login screen failed to load", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void applyAnimations() {
        try {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

            if (fadeIn != null && binding.logo != null) {
                binding.logo.startAnimation(fadeIn);
            }

            if (slideUp != null) {
                if (binding.tvWelcomeBack != null) binding.tvWelcomeBack.startAnimation(slideUp);
                if (binding.tilEmailMobile != null) binding.tilEmailMobile.startAnimation(slideUp);
                if (binding.tilPassword != null) binding.tilPassword.startAnimation(slideUp);
                if (binding.btnLogin != null) binding.btnLogin.startAnimation(slideUp);
            }

            if (fadeIn != null && binding.tvSignup != null) {
                binding.tvSignup.startAnimation(fadeIn);
            }
        } catch (Exception e) {
            Log.e(TAG, "Animation error", e);
        }
    }

    private void setupLoginActions() {
        try {
            String prefillEmail = getIntent().getStringExtra("prefill_email");
            if (prefillEmail != null && binding.etEmailMobile != null) {
                binding.etEmailMobile.setText(prefillEmail);
            }

            binding.btnLogin.setOnClickListener(v -> {
                AnimUtils.pressAnimation(v);
                validateAndLogin();
            });

            binding.tvSignup.setOnClickListener(v -> {
                AnimUtils.pressAnimation(v);
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            });

            binding.tvForgotPassword.setOnClickListener(v -> {
                AnimUtils.pressAnimation(v);
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            });

        } catch (Exception e) {
            Log.e(TAG, "Setup error", e);
        }
    }

    private void validateAndLogin() {
        String email = binding.etEmailMobile.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        boolean isValid = true;

        if (email.isEmpty()) {
            binding.tilEmailMobile.setError("Email required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailMobile.setError("Invalid email");
            isValid = false;
        } else {
            binding.tilEmailMobile.setError(null);
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password required");
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        if (isValid) {
            performLogin(email, password);
        }
    }

    private void performLogin(String email, String password) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchAndSaveProfile(user.getUid(), email, password);
                        }
                    } else {
                        handleLoginException(task.getException());
                        AnimUtils.shake(binding.btnLogin);
                    }
                });
    }

    private void handleLoginException(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchAndSaveProfile(String uid, String email, String password) {
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    prefs.saveUser(
                            user.getName(),
                            user.getEmail(),
                            password,
                            user.getMobile(),
                            user.getProfileImageUri()
                    );
                }
                prefs.setCurrentUser(email);
                prefs.setLoggedIn(true);
                navigateToDashboard();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                prefs.setCurrentUser(email);
                prefs.setLoggedIn(true);
                navigateToDashboard();
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
