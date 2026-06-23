package com.example.agriguard.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.agriguard.R;
import com.example.agriguard.activities.LoginActivity;
import com.example.agriguard.databinding.FragmentProfileBinding;
import com.example.agriguard.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private User currentUser;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Glide.with(this).load(uri).into(binding.ivProfileImage);
                    if (currentUser != null) {
                        currentUser.setProfileImage(uri.toString());
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        userId = firebaseUser.getUid();
        mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        loadProfileData();
        loadStats();
        setupListeners();
    }

    private void setupListeners() {
        binding.btnEditProfile.setOnClickListener(v -> toggleEditMode(true));
        binding.btnCancel.setOnClickListener(v -> {
            toggleEditMode(false);
            displayUserData(); // Restore original data
        });
        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.fabEditImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnDeleteAccount.setOnClickListener(v -> {
            // Add confirmation dialog in real implementation
            Toast.makeText(getContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProfileData() {
        mDatabase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    displayUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStats() {
        // Count joined communities
        mDatabase.child("users").child(userId).child("joinedCommunities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    binding.tvCommunityCount.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Count detection history
        mDatabase.child("users").child(userId).child("history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {
                    binding.tvHistoryCount.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayUserData() {
        binding.tvDisplayName.setText(currentUser.getName());
        binding.tvDisplayEmail.setText(currentUser.getEmail());
        binding.etName.setText(currentUser.getName());
        binding.etEmail.setText(currentUser.getEmail());
        binding.etPhone.setText(currentUser.getPhone());
        binding.etLocation.setText(currentUser.getLocation());
        binding.etBio.setText(currentUser.getBio());
        binding.etCrops.setText(currentUser.getPreferredCrops());

        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfileImage())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.ivProfileImage);
        }
    }

    private void toggleEditMode(boolean edit) {
        isEditMode = edit;
        binding.tilName.setEnabled(edit);
        binding.tilPhone.setEnabled(edit);
        binding.tilLocation.setEnabled(edit);
        binding.tilBio.setEnabled(edit);
        binding.tilCrops.setEnabled(edit);
        
        binding.btnEditProfile.setVisibility(edit ? View.GONE : View.VISIBLE);
        binding.editActions.setVisibility(edit ? View.VISIBLE : View.GONE);
        binding.fabEditImage.setVisibility(edit ? View.VISIBLE : View.GONE);
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();
        String crops = binding.etCrops.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tilName.setError("Name is required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("location", location);
        updates.put("bio", bio);
        updates.put("preferredCrops", crops);
        updates.put("updatedAt", System.currentTimeMillis());
        
        if (currentUser.getProfileImage() != null) {
            updates.put("profileImage", currentUser.getProfileImage());
        }

        binding.btnSave.setEnabled(false);
        mDatabase.child("users").child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(getContext(), "✅ Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    toggleEditMode(false);
                })
                .addOnFailureListener(e -> {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(getContext(), "❌ Unable to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
