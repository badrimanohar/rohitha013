package com.example.agrinova;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
 * Enhanced Community Chat Activity with robust Camera and Gallery support.
 */
public class CommunityChatActivity extends AppCompatActivity {

    private static final String TAG = "AgriNova_CommChat";
    private static final int REQUEST_PERMISSIONS = 1004;

    private ActivityCommunityChatBinding binding;
    private ChatAdapterV2 adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private String communityId, communityName;
    private String currentUserId, currentUserName, currentUserProfile;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private ChatMessage replyingTo = null;
    private String currentPhotoPath;

    // --- ActivityResult Launchers ---

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) uploadMedia(uri); });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && currentPhotoPath != null) {
                    uploadMedia(Uri.fromFile(new File(currentPhotoPath)));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
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

            checkMembership();
            setupChatList();
            fetchUserData();
            listenForMessages();
            setupInput();
            updateUserPresence();

            if (savedInstanceState != null) {
                currentPhotoPath = savedInstanceState.getString("photo_path");
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate Error", e);
            finish();
        }
    }

    private void checkMembership() {
        if (currentUserId == null) { finish(); return; }
        mDatabase.child("communities").child(communityId).child("members").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(CommunityChatActivity.this, "Membership required", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
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
                        if (!msg.getSenderId().equals(currentUserId) && !msg.isSeen()) {
                            ds.getRef().child("seen").setValue(true);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) binding.rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupChatList() {
        adapter = new ChatAdapterV2(messageList, new ChatAdapterV2.OnMessageClickListener() {
            @Override public void onMessageLongClick(ChatMessage m) { showOptionsDialog(m); }
            @Override public void onImageClick(String url) {}
        });
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(lm);
        binding.rvChatMessages.setAdapter(adapter);
    }

    private void setupInput() {
        binding.fabSend.setOnClickListener(v -> {
            String t = binding.etMessage.getText().toString().trim();
            if (!t.isEmpty()) sendMessage(t, null, null);
        });
        binding.btnAttachMedia.setOnClickListener(v -> checkAndRequestPermissions(false));
        binding.btnCamera.setOnClickListener(v -> checkAndRequestPermissions(true));
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
            }
        }
    }

    private void launchGallery() {
        try { galleryLauncher.launch("image/*"); } catch (Exception e) { Log.e(TAG, "Gallery error", e); }
    }

    private void launchCamera() {
        try {
            File file = createImageFile();
            if (file != null) {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                cameraLauncher.launch(uri);
            }
        } catch (Exception e) { Log.e(TAG, "Camera error", e); }
    }

    private File createImageFile() throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) return null;
        File image = File.createTempFile("JPEG_" + ts, ".jpg", dir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void sendMessage(String text, String url, String type) {
        String id = mDatabase.child("communityChats").child(communityId).push().getKey();
        ChatMessage m = new ChatMessage(id, currentUserId, currentUserName, text, System.currentTimeMillis());
        m.setProfileImage(currentUserProfile); m.setMediaUrl(url); m.setMediaType(type);

        if (replyingTo != null) {
            m.setReplyId(replyingTo.getMessageId()); m.setReplyMessage(replyingTo.getMessage()); m.setReplySender(replyingTo.getSenderName());
            cancelReply();
        }

        if (id != null) mDatabase.child("communityChats").child(communityId).child(id).setValue(m);
        binding.etMessage.setText("");
    }

    private void uploadMedia(Uri uri) {
        binding.loadingChat.setVisibility(View.VISIBLE);
        String type = getContentResolver().getType(uri);
        String ext = type != null && type.contains("pdf") ? ".pdf" : ".jpg";
        StorageReference ref = mStorage.child("media/" + System.currentTimeMillis() + ext);

        ref.putFile(uri).addOnSuccessListener(ts -> ref.getDownloadUrl().addOnSuccessListener(dUri -> {
            binding.loadingChat.setVisibility(View.GONE);
            String mType = (type != null && type.contains("pdf")) ? "pdf" : "image";
            sendMessage(mType.equals("pdf") ? "Document" : "Photo", dUri.toString(), mType);
        })).addOnFailureListener(e -> {
            binding.loadingChat.setVisibility(View.GONE);
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void showOptionsDialog(ChatMessage m) {
        String[] opts = m.getSenderId().equals(currentUserId) ? new String[]{"Reply", "Delete"} : new String[]{"Reply"};
        new AlertDialog.Builder(this).setItems(opts, (d, w) -> {
            if (opts[w].equals("Reply")) setupReply(m);
            else deleteMessage(m);
        }).show();
    }

    private void deleteMessage(ChatMessage m) {
        mDatabase.child("communityChats").child(communityId).child(m.getMessageId()).removeValue();
    }

    private void setupReply(ChatMessage m) {
        replyingTo = m;
        binding.layoutReplyPreview.setVisibility(View.VISIBLE);
        binding.tvReplyName.setText("Replying to " + m.getSenderName());
        binding.tvReplyText.setText(m.getMessage());
        binding.etMessage.requestFocus();
    }

    private void cancelReply() { replyingTo = null; binding.layoutReplyPreview.setVisibility(View.GONE); }

    private void updateUserPresence() {
        if (currentUserId == null) return;
        DatabaseReference r = mDatabase.child("users").child(currentUserId).child("status");
        FirebaseDatabase.getInstance().getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                if (Boolean.TRUE.equals(s.getValue(Boolean.class))) {
                    r.setValue("online"); r.onDisconnect().setValue("offline");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", currentPhotoPath);
    }
}
