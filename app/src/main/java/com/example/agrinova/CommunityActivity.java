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
import com.example.agrinova.databinding.ActivityCommunityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private ActivityCommunityBinding binding;
    private CommunityAdapterV2 exploreAdapter;
    private MyCommunityAdapter joinedAdapter;
    private List<Community> allCommunities = new ArrayList<>();
    private List<Community> joinedList = new ArrayList<>();
    private List<Community> exploreList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        
        mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();

        setupRecyclerViews();
        fetchCommunities();
        setupSearch();
    }

    private void setupRecyclerViews() {
        // Explore Adapter
        exploreAdapter = new CommunityAdapterV2(exploreList, community -> {
            joinCommunity(community);
        });
        binding.rvCommunities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCommunities.setAdapter(exploreAdapter);

        // Joined Adapter
        joinedAdapter = new MyCommunityAdapter(joinedList, community -> {
            Intent intent = new Intent(this, CommunityChatActivity.class);
            intent.putExtra("communityId", community.getId());
            intent.putExtra("communityName", community.getName());
            startActivity(intent);
        });
        binding.rvJoinedCommunities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedCommunities.setAdapter(joinedAdapter);
    }

    private void fetchCommunities() {
        binding.progressBar.setVisibility(View.VISIBLE);
        mDatabase.child("communities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCommunities.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Community community = ds.getValue(Community.class);
                        if (community != null) {
                            community.setId(ds.getKey());
                            allCommunities.add(community);
                        }
                    }
                } else {
                    seedInitialCommunities();
                }
                updateLists("");
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateLists(String query) {
        joinedList.clear();
        exploreList.clear();

        for (Community c : allCommunities) {
            boolean isMember = currentUserId != null && c.getMembers() != null && c.getMembers().containsKey(currentUserId);
            boolean matchesSearch = query.isEmpty() || c.getName().toLowerCase().contains(query.toLowerCase());

            if (matchesSearch) {
                if (isMember) {
                    joinedList.add(c);
                } else {
                    exploreList.add(c);
                }
            }
        }

        // Update Visibility
        binding.tvJoinedHeader.setVisibility(joinedList.isEmpty() ? View.GONE : View.VISIBLE);
        binding.rvJoinedCommunities.setVisibility(joinedList.isEmpty() ? View.GONE : View.VISIBLE);
        
        binding.tvExploreHeader.setVisibility(exploreList.isEmpty() ? View.GONE : View.VISIBLE);
        binding.rvCommunities.setVisibility(exploreList.isEmpty() ? View.GONE : View.VISIBLE);

        joinedAdapter.notifyDataSetChanged();
        exploreAdapter.notifyDataSetChanged();
    }

    private void seedInitialCommunities() {
        String[] crops = {
            "Rice", "Wheat", "Maize", "Cotton", "Sugarcane", "Groundnut", "Soybean", "Chilli",
            "Potato", "Tomato", "Mustard", "Bajra", "Jowar", "Blackgram", "Redgram", "Onion",
            "Garlic", "Turmeric", "Ginger", "Cabbage", "Cauliflower", "Brinjal", "Okra",
            "Cucumber", "Sunflower", "Coconut", "Banana", "Mango", "Papaya", "Pomegranate"
        };
        for (String crop : crops) {
            String id = crop.toLowerCase();
            Community community = new Community(id, crop + " Community", "A group for " + crop + " farmers.", "Crop", R.drawable.ic_logo);
            community.setMemberCount(0);
            mDatabase.child("communities").child(id).setValue(community);
        }
    }

    private void joinCommunity(Community community) {
        if (currentUserId == null) return;

        mDatabase.child("communities").child(community.getId()).child("members").child(currentUserId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Update user's joined communities
                    mDatabase.child("userCommunities").child(currentUserId).child(community.getId()).setValue(true);
                    
                    // Increment member count using transaction
                    mDatabase.child("communities").child(community.getId()).child("memberCount")
                            .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                                @NonNull
                                @Override
                                public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                                    Long count = currentData.getValue(Long.class);
                                    if (count == null) {
                                        currentData.setValue(1);
                                    } else {
                                        currentData.setValue(count + 1);
                                    }
                                    return com.google.firebase.database.Transaction.success(currentData);
                                }

                                @Override
                                public void onComplete(@com.google.firebase.database.annotations.Nullable com.google.firebase.database.DatabaseError error, boolean committed, @com.google.firebase.database.annotations.Nullable DataSnapshot currentData) {
                                }
                            });

                    Toast.makeText(this, "You have successfully joined " + community.getName() + " Community.", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(this, CommunityChatActivity.class);
                    intent.putExtra("communityId", community.getId());
                    intent.putExtra("communityName", community.getName());
                    startActivity(intent);
                });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLists(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
