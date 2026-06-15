package com.example.agrinova;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agrinova.databinding.ActivityCommunityMembersBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CommunityMembersActivity extends AppCompatActivity {

    private ActivityCommunityMembersBinding binding;
    private MemberAdapter adapter;
    private List<User> memberList = new ArrayList<>();
    private String communityId;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        communityId = getIntent().getStringExtra("communityId");
        mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        adapter = new MemberAdapter(memberList);
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(adapter);

        fetchMembers();
    }

    private void fetchMembers() {
        mDatabase.child("communities").child(communityId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String userId = ds.getKey();
                    if (userId != null) {
                        fetchUserDetails(userId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchUserDetails(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    memberList.add(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
