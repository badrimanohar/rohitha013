package com.example.agriguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agriguard.R;
import com.example.agriguard.adapters.HistoryAdapter;
import com.example.agriguard.models.PredictionResult;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.MenuItem;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private DatabaseReference mDatabase;
    private String userId;
    private List<PredictionResult> currentHistory = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Detection History");
        toolbar.inflateMenu(R.menu.history_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_all) {
                showDeleteAllConfirmation();
                return true;
            }
            return false;
        });
        
        rvHistory = view.findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new HistoryAdapter(new ArrayList<>(), this::showDeleteConfirmation);
        rvHistory.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        loadHistory();

        return view;
    }

    private void loadHistory() {
        if (userId == null) return;

        mDatabase.child("users").child(userId).child("history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PredictionResult> historyList = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        PredictionResult result = postSnapshot.getValue(PredictionResult.class);
                        if (result != null) {
                            historyList.add(result);
                        }
                    } catch (Exception e) {
                        // Skip records with invalid data types and log the error
                        android.util.Log.e("HISTORY_CRASH_FIX", "Failed to parse record: " + postSnapshot.getKey(), e);
                    }
                }
                Collections.reverse(historyList); // Most recent first
                currentHistory = historyList;
                adapter.updateData(historyList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteConfirmation(PredictionResult result) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete History")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Delete", (dialog, which) -> deleteHistoryItem(result))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteHistoryItem(PredictionResult result) {
        if (userId != null && result.getId() != null) {
            mDatabase.child("users").child(userId).child("history").child(result.getId())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showDeleteAllConfirmation() {
        if (currentHistory.isEmpty()) {
            Toast.makeText(getContext(), "No history to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all detection records? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> deleteAllHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAllHistory() {
        if (userId != null) {
            mDatabase.child("users").child(userId).child("history").removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "All history cleared", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to clear history", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
