package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agrinova.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnReset.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            validateAndReset();
        });

        binding.tvBackToLogin.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            finish();
        });
    }

    private void validateAndReset() {
        String email = binding.etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            binding.tilEmail.setError("Enter email");
            AnimUtils.shake(binding.tilEmail);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter valid email");
            AnimUtils.shake(binding.tilEmail);
            return;
        }

        binding.tilEmail.setError(null);
        performReset(email);
    }

    private void performReset(String userEmail) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnReset.setEnabled(false);

        mAuth.sendPasswordResetEmail(userEmail)
            .addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnReset.setEnabled(true);

                if (task.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                    builder.setTitle("Email Sent");
                    builder.setMessage("Password reset link has been sent to your Inbox.\n\nIf not found, wait a few minutes and refresh mail.");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                        finish();
                    });
                    builder.setCancelable(false);
                    builder.show();
                } else {
                    handleResetError(task.getException());
                }
            });
    }

    private void handleResetError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(this, "No account found with this email", Toast.LENGTH_LONG).show();
            binding.tilEmail.setError("Account not found");
        } else if (e != null && e.getMessage() != null && e.getMessage().toLowerCase().contains("network")) {
            Toast.makeText(this, "Check your internet connection", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Unable to send reset email", Toast.LENGTH_LONG).show();
        }
        AnimUtils.shake(binding.btnReset);
    }
}