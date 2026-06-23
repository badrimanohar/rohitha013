package com.example.agriguard.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agriguard.R;
import com.example.agriguard.adapters.MessageAdapter;
import com.example.agriguard.models.Community;
import com.example.agriguard.models.ChatMessage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import android.view.Menu;
import android.view.MenuItem;
import com.example.agriguard.viewmodels.CommunityViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CommunityChatActivity extends AppCompatActivity {

    private String communityId, communityName;
    private DatabaseReference mDatabase;
    private MessageAdapter adapter;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private RecyclerView rvMessages;
    private FirebaseUser currentUser;
    private CommunityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        communityId = getIntent().getStringExtra("communityId");
        communityName = getIntent().getStringExtra("communityName");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (communityId == null || currentUser == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);

        initViews();
        setupRecyclerView();
        loadMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_leave) {
            showLeaveDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLeaveDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Leave Community")
                .setMessage("Are you sure you want to leave this community?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    mDatabase.child("communities").child(communityId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Community community = snapshot.getValue(Community.class);
                            if (community != null) {
                                community.setId(snapshot.getKey());
                                viewModel.leaveCommunity(community);
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(communityName);

        etMessage = findViewById(R.id.et_message);
        rvMessages = findViewById(R.id.rv_chat_messages);

        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void loadMessages() {
        mDatabase.child("messages").child(communityId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ChatMessage message = postSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                adapter.updateData(messageList);
                rvMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommunityChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String messageId = mDatabase.child("messages").child(communityId).push().getKey();
        String senderName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Farmer";
        
        ChatMessage message = new ChatMessage(messageId, currentUser.getUid(), senderName, text, null, System.currentTimeMillis());

        if (messageId != null) {
            mDatabase.child("messages").child(communityId).child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        etMessage.setText("");
                        mDatabase.child("communities").child(communityId).child("lastMessage").setValue(text);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
        }
    }
}
