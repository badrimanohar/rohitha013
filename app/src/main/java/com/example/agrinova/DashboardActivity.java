package com.example.agrinova;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.example.agrinova.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private PrefsManager prefs;
    private static final String TAG = "AgriNovaDashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityDashboardBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            
            prefs = new PrefsManager(this);

            // Background sync Firebase Auth if needed
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null && prefs.isLoggedIn()) {
                PrefsManager.LocalUser user = prefs.getCurrentUser();
                if (user != null && user.email != null && user.password != null) {
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(user.email, user.password);
                }
            }

            setupDashboardActions();
            checkStoragePermissions();
            updateUserDetails();
            applyStartAnimations();

        } catch (Exception e) {
            Log.e(TAG, "Dashboard initialization failed", e);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Only redirect if NOT logged in according to Prefs AND Firebase
        if (!prefs.isLoggedIn() && com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void applyStartAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);

        if (fadeIn != null) {
            if (binding.header != null) binding.header.startAnimation(fadeIn);
            if (binding.bottomActionBar != null) binding.bottomActionBar.startAnimation(fadeIn);
        }
        
        if (scaleIn != null) {
            if (binding.cardCropPrice != null) binding.cardCropPrice.startAnimation(scaleIn);
            if (binding.cardCropSuggestion != null) binding.cardCropSuggestion.startAnimation(scaleIn);
            if (binding.cardDiseaseDetection != null) binding.cardDiseaseDetection.startAnimation(scaleIn);
            if (binding.cardCommunity != null) binding.cardCommunity.startAnimation(scaleIn);
        }
    }

    private void setupDashboardActions() {
        // Drawer toggle
        binding.btnMenu.setOnClickListener(v -> {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Sidebar navigation
        binding.navView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return handleNavigation(item.getItemId());
        });

        binding.cardCropPrice.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, CropPricePredictionActivity.class));
        });

        binding.btnCheckPrice.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, CropPricePredictionActivity.class));
        });

        binding.cardCropSuggestion.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, CropAnalysisActivity.class));
        });

        binding.btnGetSuggestion.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, CropAnalysisActivity.class));
        });

        binding.cardDiseaseDetection.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, DiseaseDetectionActivity.class));
        });

        binding.btnScanDisease.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, DiseaseDetectionActivity.class));
        });

        binding.cardCommunity.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, CommunityActivity.class));
        });
        
        binding.btnBrowseCommunities.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, CommunityActivity.class));
        });

        // Bottom Action Bar Logic (Chat, Market, Profile)
        binding.actionChat.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, MyCommunitiesActivity.class));
        });

        binding.actionHistory.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, CropQualityHistoryActivity.class));
        });

        binding.actionProfile.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            startActivity(new Intent(this, ProfileSettingsActivity.class));
        });
    }

    private void updateUserDetails() {
        String farmerName = prefs.getUserName();
        // Requirement 4 & 6: Use strings.xml placeholders instead of concatenation
        binding.tvWelcome.setText(getString(R.string.welcome_farmer, farmerName));

        // Update Sidebar/Navigation Header
        android.view.View headerView = binding.navView.getHeaderView(0);
        if (headerView != null) {
            android.widget.TextView tvNavName = headerView.findViewById(R.id.tv_nav_name);
            android.widget.TextView tvNavMobile = headerView.findViewById(R.id.tv_nav_mobile);
            android.widget.ImageView ivNavProfile = headerView.findViewById(R.id.iv_nav_profile);

            PrefsManager.LocalUser user = prefs.getCurrentUser();
            if (user != null) {
                if (tvNavName != null) tvNavName.setText(user.name);
                if (tvNavMobile != null) tvNavMobile.setText(user.mobile != null ? user.mobile : user.email);
                if (ivNavProfile != null && user.profileImageUri != null) {
                    com.bumptech.glide.Glide.with(this)
                        .load(user.profileImageUri)
                        .placeholder(R.drawable.ic_logo)
                        .into(ivNavProfile);
                }
            }
        }
    }

    private boolean handleNavigation(int id) {
        if (id == R.id.nav_dashboard) {
            // Requirement 8: Fixed empty if body
            Log.d(TAG, "Already on Dashboard");
        } else if (id == R.id.nav_community) {
            startActivity(new Intent(this, CommunityActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, ProfileSettingsActivity.class));
        } else if (id == R.id.nav_quality_history) {
            startActivity(new Intent(this, CropQualityHistoryActivity.class));
        } else if (id == R.id.nav_crop_quality) {
            startActivity(new Intent(this, DiseaseDetectionActivity.class));
        } else if (id == R.id.nav_logout) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            prefs.setLoggedIn(false);
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserDetails();
    }

    private void checkStoragePermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Permission check failed", e);
        }
    }
}
