package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agrinova.databinding.ActivityCommunitySearchBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CommunitySearchActivity extends AppCompatActivity {

    private ActivityCommunitySearchBinding binding;
    private CommunityAdapterV2 adapter;
    private List<Community> allCommunities = new ArrayList<>();
    private List<Community> filteredList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();

        setupRecyclerView();
        fetchAllCommunities();
        setupSearch();
    }

    private void setupRecyclerView() {
        adapter = new CommunityAdapterV2(filteredList, community -> {
            joinCommunity(community);
        });
        binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResults.setAdapter(adapter);
    }

    private void fetchAllCommunities() {
        mDatabase.child("communities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCommunities.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Community community = ds.getValue(Community.class);
                    if (community != null) {
                        community.setId(ds.getKey());
                        allCommunities.add(community);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        filteredList.clear();
        if (!query.isEmpty()) {
            for (Community c : allCommunities) {
                if (c.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void joinCommunity(Community community) {
        if (currentUserId == null) return;
        mDatabase.child("communities").child(community.getId()).child("members").child(currentUserId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    mDatabase.child("userCommunities").child(currentUserId).child(community.getId()).setValue(true);
                    Toast.makeText(this, "Joined " + community.getName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, CommunityChatActivity.class);
                    intent.putExtra("communityId", community.getId());
                    intent.putExtra("communityName", community.getName());
                    startActivity(intent);
                });
    }
}
