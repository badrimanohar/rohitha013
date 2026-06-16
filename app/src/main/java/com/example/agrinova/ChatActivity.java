package com.example.agrinova;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Community Chat Activity.
 * Updated with robust Camera and Gallery handling to prevent crashes.
 */
public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnMessageClickListener {

    private static final String TAG = "AgriNova_Chat";
    private static final int REQUEST_PERMISSIONS = 1003;

    private ActivityCommunityChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private String communityId, communityName;
    private String currentUserId, currentUserName, currentUserProfile;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private ChatMessage replyingTo = null;
    private String currentPhotoPath;

    // --- ActivityResult Launchers ---

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && currentPhotoPath != null) {
                    uploadImage(Uri.fromFile(new File(currentPhotoPath)));
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImage(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityCommunityChatBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            communityId = getIntent().getStringExtra("communityId");
            communityName = getIntent().getStringExtra("communityName");
            
            if (communityId == null) {
                finish();
                return;
            }

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mStorage = FirebaseStorage.getInstance().getReference();
            currentUserId = mAuth.getUid();

            binding.tvChatName.setText(communityName);
            binding.btnBack.setOnClickListener(v -> finish());
            binding.layoutTitles.setOnClickListener(v -> {
                Intent intent = new Intent(this, CommunityMembersActivity.class);
                intent.putExtra("communityId", communityId);
                startActivity(intent);
            });

            setupChatList();
            fetchUserData();
            listenForMessages();
            listenForTyping();
            setupInput();

            if (savedInstanceState != null) {
                currentPhotoPath = savedInstanceState.getString("photo_path");
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate Error", e);
            finish();
        }
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
                } else {
                    currentUserName = "Farmer";
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupChatList() {
        adapter = new ChatAdapter(messageList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(adapter);
    }

    private void listenForMessages() {
        mDatabase.child("communities").child(communityId).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage msg = ds.getValue(ChatMessage.class);
                    if (msg != null) {
                        messageList.add(msg);
                    }
                }
                adapter.setMessages(messageList);
                if (!messageList.isEmpty()) {
                    binding.rvChatMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForTyping() {
        mDatabase.child("communities").child(communityId).child("typing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder typingUsers = new StringBuilder();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = ds.getKey();
                    if (uid != null && !uid.equals(currentUserId)) {
                        String name = ds.getValue(String.class);
                        if (name != null) {
                            if (typingUsers.length() > 0) typingUsers.append(", ");
                            typingUsers.append(name);
                        }
                    }
                }
                if (typingUsers.length() > 0) {
                    binding.tvChatStatus.setText(typingUsers.append(" typing...").toString());
                    binding.tvChatStatus.setVisibility(View.VISIBLE);
                } else {
                    binding.tvChatStatus.setText("online");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateTypingStatus(boolean isTyping) {
        if (currentUserId == null) return;
        if (isTyping) {
            mDatabase.child("communities").child(communityId).child("typing").child(currentUserId).setValue(currentUserName);
        } else {
            mDatabase.child("communities").child(communityId).child("typing").child(currentUserId).removeValue();
        }
    }

    private void setupInput() {
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTypingStatus(!s.toString().trim().isEmpty());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.fabSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text, null);
                updateTypingStatus(false);
            }
        });

        binding.btnCamera.setOnClickListener(v -> checkAndRequestPermissions(true));
        binding.btnAttachMedia.setOnClickListener(v -> checkAndRequestPermissions(false));
        binding.btnCancelReply.setOnClickListener(v -> cancelReply());
    }

    private void checkAndRequestPermissions(boolean isCamera) {
        String[] perms;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            perms = isCamera ? new String[]{Manifest.permission.CAMERA} : new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            perms = isCamera ? new String[]{Manifest.permission.CAMERA} : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        boolean allGranted = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false; break;
            }
        }

        if (allGranted) {
            if (isCamera) launchCamera(); else launchGallery();
        } else {
            ActivityCompat.requestPermissions(this, perms, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                boolean forCamera = false;
                for (String p : permissions) if (p.equals(Manifest.permission.CAMERA)) forCamera = true;
                if (forCamera) launchCamera(); else launchGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchGallery() {
        try { galleryLauncher.launch("image/*"); } catch (Exception e) { Log.e(TAG, "Gallery error", e); }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                cameraLauncher.launch(photoUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Camera launch error", e);
            Toast.makeText(this, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) return null;
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void sendMessage(String text, String imageUrl) {
        String msgKey = mDatabase.child("communities").child(communityId).child("messages").push().getKey();
        ChatMessage message = new ChatMessage(msgKey, currentUserId, currentUserName, text, System.currentTimeMillis());
        message.setProfileImage(currentUserProfile);
        message.setMediaUrl(imageUrl);
        if (imageUrl != null) message.setMediaType("image");

        if (replyingTo != null) {
            message.setReplyId(replyingTo.getMessageId());
            message.setReplyMessage(replyingTo.getMessage());
            message.setReplySender(replyingTo.getSenderName());
            cancelReply();
        }

        if (msgKey != null) {
            mDatabase.child("communities").child(communityId).child("messages").child(msgKey).setValue(message);
            Map<String, Object> update = new HashMap<>();
            update.put("lastMessage", imageUrl != null ? "📷 Photo" : text);
            update.put("lastMessageTime", System.currentTimeMillis());
            mDatabase.child("communities").child(communityId).updateChildren(update);
        }
        binding.etMessage.setText("");
    }

    private void uploadImage(Uri uri) {
        binding.loadingChat.setVisibility(View.VISIBLE);
        StorageReference ref = mStorage.child("chat_images/" + System.currentTimeMillis() + ".jpg");
        ref.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                binding.loadingChat.setVisibility(View.GONE);
                sendMessage("Sent an image", downloadUri.toString());
            });
        }).addOnFailureListener(e -> {
            binding.loadingChat.setVisibility(View.GONE);
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMessageLongClick(ChatMessage message) {
        replyingTo = message;
        binding.layoutReplyPreview.setVisibility(View.VISIBLE);
        binding.tvReplyName.setText("Replying to " + message.getSenderName());
        binding.tvReplyText.setText(message.getMessage());
        binding.etMessage.requestFocus();
    }

    @Override public void onImageClick(String imageUrl) {}
    @Override public void onReplyClick(ChatMessage message) {}

    private void cancelReply() {
        replyingTo = null;
        binding.layoutReplyPreview.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", currentPhotoPath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateTypingStatus(false);
    }
}
