package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agrinova.databinding.ActivityMyCommunitiesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MyCommunitiesActivity extends AppCompatActivity {

    private ActivityMyCommunitiesBinding binding;
    private MyCommunityAdapter adapter;
    private List<Community> myCommunities = new ArrayList<>();
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyCommunitiesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();

        setupRecyclerView();
        fetchMyCommunities();
    }

    private void setupRecyclerView() {
        adapter = new MyCommunityAdapter(myCommunities, community -> {
            Intent intent = new Intent(this, CommunityChatActivity.class);
            intent.putExtra("communityId", community.getId());
            intent.putExtra("communityName", community.getName());
            startActivity(intent);
        });
        binding.rvMyCommunities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyCommunities.setAdapter(adapter);
    }

    private void fetchMyCommunities() {
        if (currentUserId == null) return;

        mDatabase.child("userCommunities").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myCommunities.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String communityId = ds.getKey();
                        if (communityId != null) fetchCommunityDetails(communityId);
                    }
                } else {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchCommunityDetails(String id) {
        mDatabase.child("communities").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Community community = snapshot.getValue(Community.class);
                if (community != null) {
                    community.setId(snapshot.getKey());
                    myCommunities.add(community);
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
