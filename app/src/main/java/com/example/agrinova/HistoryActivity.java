package com.example.agrinova;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrinova.databinding.ActivityHistoryBinding;
import com.example.agrinova.databinding.ItemHistoryBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private PrefsManager prefs;
    private PredictionHistoryAdapter adapter;
    private List<PredictionRecord> recordList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new PrefsManager(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupFirebaseList();
    }

    private void setupFirebaseList() {
        if (mAuth.getCurrentUser() == null) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        recordList = new ArrayList<>();
        adapter = new PredictionHistoryAdapter(recordList);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);

        fetchHistory();
    }

    private void fetchHistory() {
        String userId = mAuth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(userId).collection("prediction_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                binding.progressBar.setVisibility(View.GONE);
                recordList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    PredictionRecord record = doc.toObject(PredictionRecord.class);
                    if (record != null) recordList.add(record);
                }

                if (recordList.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
            });
    }

    // Removing setupList() as we replaced it with setupFirebaseList()

}
