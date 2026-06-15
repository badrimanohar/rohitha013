package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agrinova.databinding.ActivityCommunityBrowseBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CommunityBrowseActivity extends AppCompatActivity {

    private ActivityCommunityBrowseBinding binding;
    private CommunityAdapter joinedAdapter;
    private CommunityAdapter suggestedAdapter;
    private List<Community> allCommunities = new ArrayList<>();
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityBrowseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login to access communities", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerViews();
        fetchCommunities();
        setupSearch();
    }

    private void fetchCommunities() {
        mDatabase.child("communities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCommunities.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Community community = ds.getValue(Community.class);
                        if (community != null) {
                            community.setId(ds.getKey());
                            checkIfJoined(community);
                            allCommunities.add(community);
                        }
                    }
                    // If some crops are missing, seed them
                    if (allCommunities.size() < 30) {
                        seedCommunities();
                    }
                } else {
                    seedCommunities();
                }
                updateAdapters(allCommunities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommunityBrowseActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfJoined(Community community) {
        if (community.getMembers() != null && community.getMembers().containsKey(currentUserId)) {
            community.setJoined(true);
        } else {
            community.setJoined(false);
        }
    }

    private void seedCommunities() {
        String[] crops = {
            "Rice", "Wheat", "Maize", "Cotton", "Sugarcane", "Tomato", "Potato", "Chilli", 
            "Groundnut", "Soybean", "Mustard", "Bajra", "Jowar", "Blackgram", "Redgram",
            "Mango", "Banana", "Grapes", "Citrus", "Onion", "Garlic", "Turmeric", "Ginger",
            "Cumin", "Black Pepper", "Coffee", "Tea", "Rubber", "Coconut", "Papaya", "Watermelon"
        };
        for (String name : crops) {
            String id = name.toLowerCase().replace(" ", "_");
            Community c = new Community(id, name + " Community", "Farmers discuss " + name + " farming.", "Crop", R.drawable.ic_logo);
            mDatabase.child("communities").child(id).setValue(c);
        }
    }

    private void setupRecyclerViews() {
        CommunityAdapter.OnCommunityClickListener clickListener = community -> {
            if (community.isJoined()) {
                openChat(community);
            } else {
                joinCommunity(community);
            }
        };

        joinedAdapter = new CommunityAdapter(new ArrayList<>(), clickListener);
        binding.rvJoinedCommunities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedCommunities.setAdapter(joinedAdapter);

        suggestedAdapter = new CommunityAdapter(new ArrayList<>(), clickListener);
        binding.rvSuggestedCommunities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSuggestedCommunities.setAdapter(suggestedAdapter);
    }

    private void joinCommunity(Community community) {
        mDatabase.child("communities").child(community.getId()).child("members").child(currentUserId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Joined " + community.getName(), Toast.LENGTH_SHORT).show();
                    openChat(community);
                });
    }

    private void openChat(Community community) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("communityId", community.getId());
        intent.putExtra("communityName", community.getName());
        startActivity(intent);
    }

    private void setupSearch() {
        binding.etSearchCommunity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        List<Community> filtered = new ArrayList<>();
        for (Community c : allCommunities) {
            if (c.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(c);
            }
        }
        updateAdapters(filtered);
    }

    private void updateAdapters(List<Community> list) {
        List<Community> joined = new ArrayList<>();
        List<Community> suggested = new ArrayList<>();

        for (Community c : list) {
            if (c.isJoined()) joined.add(c);
            else suggested.add(c);
        }

        joinedAdapter.updateList(joined);
        suggestedAdapter.updateList(suggested);

        binding.tvJoinedHeader.setVisibility(joined.isEmpty() ? View.GONE : View.VISIBLE);
        binding.rvJoinedCommunities.setVisibility(joined.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
