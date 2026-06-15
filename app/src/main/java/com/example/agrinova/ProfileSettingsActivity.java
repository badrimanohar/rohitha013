package com.example.agrinova;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.agrinova.databinding.ActivityProfileSettingsBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileSettingsActivity extends AppCompatActivity {

    private ActivityProfileSettingsBinding binding;
    private PrefsManager prefs;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private String selectedImageUriStr = null;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        // Copy image to internal storage for persistent access
                        String fileName = "profile_image.jpg";
                        java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                        if (inputStream != null) {
                            java.io.FileOutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE);
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            inputStream.close();
                            outputStream.close();

                            java.io.File file = new java.io.File(getFilesDir(), fileName);
                            selectedImageUriStr = Uri.fromFile(file).toString();
                            
                            if (binding != null) {
                                Glide.with(this).load(file).into(binding.ivProfileSettings);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AgriNova", "Image pick failed", e);
                        Toast.makeText(this, "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                try {
                    if (isGranted) {
                        getContent.launch("image/*");
                    } else {
                        Toast.makeText(this, "Permission denied to read images", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("AgriNova", "Permission handling failed", e);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
            storage = FirebaseStorage.getInstance();
            prefs = new PrefsManager(this);

            loadUserData();

            binding.btnSaveProfile.setOnClickListener(v -> {
                saveProfileDetails();
            });

            binding.fabEditPhoto.setOnClickListener(v -> {
                try {
                    String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                            android.Manifest.permission.READ_MEDIA_IMAGES :
                            android.Manifest.permission.READ_EXTERNAL_STORAGE;

                    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        getContent.launch("image/*");
                    } else {
                        requestPermissionLauncher.launch(permission);
                    }
                } catch (Exception e) {
                    android.util.Log.e("AgriNova", "Edit photo click failed", e);
                }
            });

            binding.cardChangeLang.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, LanguageSelectionActivity.class);
                    intent.putExtra("fromSettings", true);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("AgriNova", "Navigation failed", e);
                }
            });

            binding.btnLogout.setOnClickListener(v -> {
                try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    if (prefs != null) {
                        prefs.setLoggedIn(false);
                        prefs.setCurrentUser(null);
                    }
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("AgriNova", "Logout failed", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AgriNova", "ProfileSettingsActivity initialization failed", e);
            finish();
        }
    }

    private void loadUserData() {
        PrefsManager.LocalUser user = prefs.getCurrentUser();
        if (user != null) {
            binding.tvProfileName.setText(user.name);
            binding.tvProfileContact.setText(user.mobile != null && !user.mobile.isEmpty() ? user.mobile : user.email);
            binding.etName.setText(user.name);
            binding.etMobile.setText(user.mobile);
            
            if (user.profileImageUri != null) {
                Glide.with(this).load(user.profileImageUri).placeholder(R.drawable.ic_logo).into(binding.ivProfileSettings);
            }
        }

        String currentLang = prefs.getLanguage();
        binding.tvCurrentLang.setText(currentLang != null ? currentLang : "English");
    }

    private void saveProfileDetails() {
        if (binding.etName.getText() == null) return;
        String newName = binding.etName.getText().toString().trim();
        String newMobile = binding.etMobile.getText() != null ? binding.etMobile.getText().toString().trim() : "";

        if (newName.isEmpty()) {
            binding.etName.setError("Name cannot be empty");
            return;
        }

        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText(R.string.saving);

        if (selectedImageUriStr != null) {
            uploadProfileImage(newName, newMobile);
        } else {
            updateUserDatabase(newName, newMobile, prefs.getProfileImage());
        }
    }

    private void uploadProfileImage(String name, String mobile) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown";
        StorageReference ref = storage.getReference().child("profiles/" + userId + ".jpg");

        ref.putFile(Uri.parse(selectedImageUriStr))
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserDatabase(name, mobile, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed, saving details only", Toast.LENGTH_SHORT).show();
                    updateUserDatabase(name, mobile, prefs.getProfileImage());
                });
    }

    private void updateUserDatabase(String name, String mobile, String imageUrl) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            binding.btnSaveProfile.setEnabled(true);
            binding.btnSaveProfile.setText(R.string.save_details);
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        PrefsManager.LocalUser localUser = prefs.getCurrentUser();
        String email = localUser != null ? localUser.email : "";
        String password = localUser != null ? localUser.password : "";

        User user = new User(userId, name, email, mobile, imageUrl);

        mDatabase.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    prefs.saveUser(name, email, password, mobile, imageUrl);
                    binding.tvProfileName.setText(name);
                    binding.tvProfileContact.setText(mobile.isEmpty() ? email : mobile);
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText(R.string.save_details);
                    Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                    selectedImageUriStr = null;
                })
                .addOnFailureListener(e -> {
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText(R.string.save_details);
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
