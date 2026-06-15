package com.example.agrinova;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.agrinova.databinding.ActivitySignupBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private PrefsManager prefs;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private String selectedImageUriStr = null;
    private static final String TAG = "AgriNovaSignup";

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
                        String fileName = "profile_" + timeStamp + ".jpg";
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        if (inputStream != null) {
                            FileOutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE);
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            inputStream.close();
                            outputStream.close();

                            File file = new File(getFilesDir(), fileName);
                            selectedImageUriStr = Uri.fromFile(file).toString();
                            binding.ivProfile.setImageURI(Uri.fromFile(file));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Image saving failed", e);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivitySignupBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
            storage = FirebaseStorage.getInstance();
            prefs = new PrefsManager(this);

            setupInputListeners();

            binding.ivProfile.setOnClickListener(v -> {
                String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                        android.Manifest.permission.READ_MEDIA_IMAGES :
                        android.Manifest.permission.READ_EXTERNAL_STORAGE;

                if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    getContent.launch("image/*");
                } else {
                    requestPermissionLauncher.launch(permission);
                }
            });

            binding.btnSignup.setOnClickListener(v -> {
                AnimUtils.pressAnimation(v);
                if (validateFields()) {
                    registerUser();
                } else {
                    AnimUtils.shake(binding.btnSignup);
                }
            });

            binding.tvLogin.setOnClickListener(v -> {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            });

            applyAnimations();

        } catch (Exception e) {
            Log.e(TAG, "SignupActivity initialization failed", e);
            finish();
        }
    }

    private void setupInputListeners() {
        binding.etName.addTextChangedListener(new SimpleTextWatcher(binding.tilName));
        binding.etEmail.addTextChangedListener(new SimpleTextWatcher(binding.tilEmail));
        binding.etPassword.addTextChangedListener(new SimpleTextWatcher(binding.tilPassword));
        binding.etConfirmPassword.addTextChangedListener(new SimpleTextWatcher(binding.tilConfirmPassword));
        binding.etMobile.addTextChangedListener(new SimpleTextWatcher(binding.tilMobile));
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final com.google.android.material.textfield.TextInputLayout layout;
        SimpleTextWatcher(com.google.android.material.textfield.TextInputLayout layout) { this.layout = layout; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { layout.setError(null); }
        @Override public void afterTextChanged(Editable s) {}
    }

    private void applyAnimations() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        if (fadeIn != null && binding.ivProfile != null) binding.ivProfile.startAnimation(fadeIn);
        if (slideUp != null) {
            if (binding.tilName != null) binding.tilName.startAnimation(slideUp);
            if (binding.tilMobile != null) binding.tilMobile.startAnimation(slideUp);
            if (binding.tilEmail != null) binding.tilEmail.startAnimation(slideUp);
            if (binding.tilPassword != null) binding.tilPassword.startAnimation(slideUp);
            if (binding.tilConfirmPassword != null) binding.tilConfirmPassword.startAnimation(slideUp);
            if (binding.btnSignup != null) binding.btnSignup.startAnimation(slideUp);
        }
        if (fadeIn != null && binding.tvLogin != null) binding.tvLogin.startAnimation(fadeIn);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSignup.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.btnSignup.setEnabled(!loading);
    }

    private void registerUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String name = binding.etName.getText().toString().trim();
        String mobile = binding.etMobile.getText().toString().trim();

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        if (selectedImageUriStr != null) {
                            uploadProfileImage(userId, name, email, password, mobile);
                        } else {
                            saveUserToDatabase(userId, name, email, null, password, mobile);
                        }
                    } else {
                        setLoading(false);
                        handleSignupError(task.getException(), email);
                    }
                });
    }

    private void handleSignupError(Exception e, String email) {
        AnimUtils.shake(binding.btnSignup);
        if (e instanceof FirebaseAuthUserCollisionException) {
            binding.tilEmail.setError(getString(R.string.error_email_registered));
            Snackbar.make(binding.getRoot(), getString(R.string.error_email_registered), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.login), v -> {
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        intent.putExtra("prefill_email", email);
                        startActivity(intent);
                        finish();
                    }).show();
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            binding.tilPassword.setError(getString(R.string.error_weak_password));
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
        } else if (e != null && e.getMessage() != null && e.getMessage().toLowerCase().contains("network")) {
            Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
        } else {
            String error = e != null ? e.getMessage() : "Unknown";
            Toast.makeText(this, getString(R.string.error_signup_failed, error), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadProfileImage(String userId, String name, String email, String password, String mobile) {
        StorageReference ref = storage.getReference().child("profiles/" + userId + ".jpg");
        ref.putFile(Uri.parse(selectedImageUriStr))
            .addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveUserToDatabase(userId, name, email, uri.toString(), password, mobile);
                });
            })
            .addOnFailureListener(e -> {
                saveUserToDatabase(userId, name, email, null, password, mobile);
            });
    }

    private void saveUserToDatabase(String userId, String name, String email, String imgUrl, String password, String mobile) {
        User user = new User(userId, name, email, mobile, imgUrl);

        mDatabase.child("users").child(userId).setValue(user)
            .addOnSuccessListener(aVoid -> {
                setLoading(false);
                prefs.saveUser(name, email, password, mobile, imgUrl);
                prefs.setLoggedIn(true);
                Toast.makeText(this, getString(R.string.signup_success_login), Toast.LENGTH_LONG).show();
                
                Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show();
            });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) getContent.launch("image/*");
            });

    private boolean validateFields() {
        boolean valid = true;
        
        String name = binding.etName.getText().toString().trim();
        String mobile = binding.etMobile.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) { binding.tilName.setError(getString(R.string.required)); valid = false; }
        if (mobile.isEmpty()) { binding.tilMobile.setError(getString(R.string.required)); valid = false; }
        else if (mobile.length() < 10) { binding.tilMobile.setError(getString(R.string.invalid_mobile)); valid = false; }
        
        if (email.isEmpty()) { binding.tilEmail.setError(getString(R.string.email_required_reset)); valid = false; }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.tilEmail.setError(getString(R.string.error_invalid_email)); valid = false; }
        
        if (password.isEmpty()) { binding.tilPassword.setError(getString(R.string.password)); valid = false; }
        else if (password.length() < 6) { binding.tilPassword.setError(getString(R.string.min_characters)); valid = false; }
        
        if (!password.equals(confirmPassword)) { binding.tilConfirmPassword.setError(getString(R.string.passwords_do_not_match)); valid = false; }

        return valid;
    }
}
