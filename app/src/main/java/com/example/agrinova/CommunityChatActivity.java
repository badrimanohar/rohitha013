package com.example.agrinova;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agrinova.databinding.ActivityCommunityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import android.provider.MediaStore;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class CommunityChatActivity extends AppCompatActivity {

    private ActivityCommunityChatBinding binding;
    private ChatAdapterV2 adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private String communityId, communityName;
    private String currentUserId, currentUserName, currentUserProfile;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private ChatMessage replyingTo = null;
    private String currentPhotoPath;

    private final ActivityResultLauncher<String> getContentLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadMedia(uri);
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && currentPhotoPath != null) {
                    uploadMedia(Uri.fromFile(new File(currentPhotoPath)));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        communityId = getIntent().getStringExtra("communityId");
        communityName = getIntent().getStringExtra("communityName");

        mDatabase = FirebaseDatabase.getInstance("https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();

        binding.tvChatName.setText(communityName);
        binding.btnBack.setOnClickListener(v -> finish());
        binding.layoutTitles.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityMembersActivity.class);
            intent.putExtra("communityId", communityId);
            startActivity(intent);
        });

        mDatabase.child("communities").child(communityId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Community community = snapshot.getValue(Community.class);
                if (community != null) {
                    binding.tvChatStatus.setText(community.getMemberCount() + " Members");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        checkMembership();
        setupChatList();
        fetchUserData();
        listenForMessages();
        setupInput();
        updateUserPresence();
    }

    private void checkMembership() {
        if (currentUserId == null) {
            finish();
            return;
        }
        mDatabase.child("communities").child(communityId).child("members").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(CommunityChatActivity.this, "You must join this community to view chat.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateUserPresence() {
        if (currentUserId == null) return;
        DatabaseReference presenceRef = mDatabase.child("users").child(currentUserId).child("status");
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    presenceRef.setValue("online");
                    presenceRef.onDisconnect().setValue("offline");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupChatList() {
        adapter = new ChatAdapterV2(messageList, new ChatAdapterV2.OnMessageClickListener() {
            @Override
            public void onMessageLongClick(ChatMessage message) {
                showOptionsDialog(message);
            }

            @Override
            public void onImageClick(String imageUrl) {
                // Handle image zoom/preview
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(adapter);
    }

    private void showOptionsDialog(ChatMessage message) {
        String[] options;
        if (message.getSenderId().equals(currentUserId)) {
            options = new String[]{"Reply", "Delete Message"};
        } else {
            options = new String[]{"Reply"};
        }

        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (options[which].equals("Reply")) {
                        setupReply(message);
                    } else if (options[which].equals("Delete Message")) {
                        deleteMessage(message);
                    }
                }).show();
    }

    private void deleteMessage(ChatMessage message) {
        mDatabase.child("communityChats").child(communityId).child(message.getMessageId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show());
    }

    private void fetchUserData() {
        if (currentUserId == null) return;
        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    currentUserName = user.getName();
                    currentUserProfile = user.getProfileImageUri();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForMessages() {
        mDatabase.child("communityChats").child(communityId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage msg = ds.getValue(ChatMessage.class);
                    if (msg != null) {
                        messageList.add(msg);
                        // Mark as seen if not from current user
                        if (!msg.getSenderId().equals(currentUserId) && !msg.isSeen()) {
                            ds.getRef().child("seen").setValue(true);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    binding.rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupInput() {
        binding.fabSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text, null, null);
            }
        });

        binding.btnAttachMedia.setOnClickListener(v -> getContentLauncher.launch("*/*"));
        binding.btnCamera.setOnClickListener(v -> openCamera());
        binding.btnCancelReply.setOnClickListener(v -> cancelReply());
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureLauncher.launch(photoURI);
            }
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setupReply(ChatMessage message) {
        replyingTo = message;
        binding.layoutReplyPreview.setVisibility(View.VISIBLE);
        binding.tvReplyName.setText("Replying to " + message.getSenderName());
        binding.tvReplyText.setText(message.getMessage());
        binding.etMessage.requestFocus();
    }

    private void cancelReply() {
        replyingTo = null;
        binding.layoutReplyPreview.setVisibility(View.GONE);
    }

    private void sendMessage(String text, String mediaUrl, String mediaType) {
        String msgId = mDatabase.child("communityChats").child(communityId).push().getKey();
        if (msgId == null) return;

        ChatMessage message = new ChatMessage(msgId, currentUserId, currentUserName, text, System.currentTimeMillis());
        message.setProfileImage(currentUserProfile);
        message.setMediaUrl(mediaUrl);
        message.setMediaType(mediaType);

        if (replyingTo != null) {
            message.setReplyId(replyingTo.getMessageId());
            message.setReplyMessage(replyingTo.getMessage());
            message.setReplySender(replyingTo.getSenderName());
            cancelReply();
        }

        mDatabase.child("communityChats").child(communityId).child(msgId).setValue(message);
        binding.etMessage.setText("");
    }

    private void uploadMedia(Uri uri) {
        binding.loadingChat.setVisibility(View.VISIBLE);
        String type = getContentResolver().getType(uri);
        String ext = type != null && type.contains("pdf") ? ".pdf" : ".jpg";
        StorageReference ref = mStorage.child("communityMedia/" + communityId + "/" + System.currentTimeMillis() + ext);

        ref.putFile(uri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            binding.loadingChat.setVisibility(View.GONE);
            String mediaType = type != null && type.contains("pdf") ? "pdf" : "image";
            String downloadUrl = downloadUri.toString();
            
            // Save to communityMedia node as requested
            String mediaId = mDatabase.child("communityMedia").child(communityId).push().getKey();
            if (mediaId != null) {
                java.util.Map<String, Object> mediaData = new java.util.HashMap<>();
                mediaData.put("fileUrl", downloadUrl);
                mediaData.put("fileType", mediaType);
                mediaData.put("senderName", currentUserName);
                mDatabase.child("communityMedia").child(communityId).child(mediaId).setValue(mediaData);
            }

            sendMessage(mediaType.equals("pdf") ? "Shared a document" : "Shared an image", downloadUrl, mediaType);
        })).addOnFailureListener(e -> {
            binding.loadingChat.setVisibility(View.GONE);
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }
}
