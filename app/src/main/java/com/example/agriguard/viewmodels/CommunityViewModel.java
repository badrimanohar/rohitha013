package com.example.agriguard.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.agriguard.models.Community;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CommunityViewModel extends ViewModel {

    private final DatabaseReference mDatabase;
    private final String userId;

    private final MutableLiveData<List<Community>> exploreCommunities = new MutableLiveData<>();
    private final MutableLiveData<List<Community>> joinedCommunities = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private List<Community> allJoinedList = new ArrayList<>();
    private List<Community> allExploreList = new ArrayList<>();

    public CommunityViewModel() {
        mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        userId = FirebaseAuth.getInstance().getUid();
        checkAndInitializeCommunities();
        fetchCommunities();
    }

    private void checkAndInitializeCommunities() {
        mDatabase.child("communities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    initializeCommunities();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initializeCommunities() {
        String[] cropNames = {"Rice", "Wheat", "Maize", "Paddy", "Cotton", "Sugarcane", "Banana", "Apple", "Mango", "Orange", "Lemon", "Grapes", "Coconut", "Tea", "Coffee", "Groundnut", "Soybean", "Potato", "Onion", "Tomato", "Chilli", "Turmeric", "Garlic", "Sunflower", "Pulses", "Millets"};
        for (String name : cropNames) {
            String id = mDatabase.child("communities").push().getKey();
            Community c = new Community(id, name + " Community", "Connect with " + name + " farmers across India to discuss best practices, pest control, and market prices.", 0, null);
            if (id != null) {
                mDatabase.child("communities").child(id).setValue(c);
            }
        }
    }

    public LiveData<List<Community>> getExploreCommunities() { return exploreCommunities; }
    public LiveData<List<Community>> getJoinedCommunities() { return joinedCommunities; }
    public LiveData<String> getError() { return error; }

    public void fetchCommunities() {
        if (userId == null) return;

        mDatabase.child("communities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Community> allCommunities = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Community community = postSnapshot.getValue(Community.class);
                    if (community != null) {
                        community.setId(postSnapshot.getKey());
                        allCommunities.add(community);
                    }
                }
                checkJoinedStatus(allCommunities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error.setValue(databaseError.getMessage());
            }
        });
    }

    private void checkJoinedStatus(List<Community> allCommunities) {
        mDatabase.child("users").child(userId).child("joinedCommunities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Community> joined = new ArrayList<>();
                List<Community> explore = new ArrayList<>();

                for (Community community : allCommunities) {
                    if (snapshot.hasChild(community.getId())) {
                        community.setJoined(true);
                        joined.add(community);
                    } else {
                        community.setJoined(false);
                        explore.add(community);
                    }
                }
                joinedCommunities.setValue(joined);
                exploreCommunities.setValue(explore);
                allJoinedList = joined;
                allExploreList = explore;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error.setValue(databaseError.getMessage());
            }
        });
    }

    public void setSearchQuery(String query) {
        if (query.isEmpty()) {
            joinedCommunities.setValue(allJoinedList);
            exploreCommunities.setValue(allExploreList);
            return;
        }

        String filter = query.toLowerCase();
        List<Community> filteredJoined = new ArrayList<>();
        for (Community c : allJoinedList) {
            if (c.getName().toLowerCase().contains(filter)) filteredJoined.add(c);
        }
        
        List<Community> filteredExplore = new ArrayList<>();
        for (Community c : allExploreList) {
            if (c.getName().toLowerCase().contains(filter)) filteredExplore.add(c);
        }

        joinedCommunities.setValue(filteredJoined);
        exploreCommunities.setValue(filteredExplore);
    }

    public void joinCommunity(Community community) {
        if (userId == null) return;

        mDatabase.child("users").child(userId).child("joinedCommunities").child(community.getId()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mDatabase.child("communities").child(community.getId()).child("memberCount")
                                .setValue(community.getMemberCount() + 1);
                    } else {
                        error.setValue("Failed to join community");
                    }
                });
    }

    public void leaveCommunity(Community community) {
        if (userId == null) return;

        mDatabase.child("users").child(userId).child("joinedCommunities").child(community.getId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mDatabase.child("communities").child(community.getId()).child("memberCount")
                                .setValue(Math.max(0, community.getMemberCount() - 1));
                    } else {
                        error.setValue("Failed to leave community");
                    }
                });
    }
}
