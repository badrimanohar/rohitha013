package com.example.agrinova;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.agrinova.databinding.ActivityHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CropQualityHistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private List<CropQuality> fullList;
    private List<CropQuality> filteredList;
    private CropQualityHistoryAdapter adapter;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private static final String DATABASE_URL = "https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("CropQuality");

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle("Quality History");
        
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        fullList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new CropQualityHistoryAdapter(filteredList);
        binding.rvHistory.setAdapter(adapter);

        setupSearch();
        fetchHistory();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        if (query.isEmpty()) {
            filteredList.clear();
            filteredList.addAll(fullList);
        } else {
            String q = query.toLowerCase();
            filteredList.clear();
            for (CropQuality d : fullList) {
                if ((d.getCropName() != null && d.getCropName().toLowerCase().contains(q)) ||
                    (d.getQualityGrade() != null && d.getQualityGrade().toLowerCase().contains(q))) {
                    filteredList.add(d);
                }
            }
        }
        adapter.notifyDataSetChanged();
        
        if (filteredList.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void fetchHistory() {
        if (mAuth.getCurrentUser() == null) return;

        String currentUserId = mAuth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        reference.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                fullList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    CropQuality q = data.getValue(CropQuality.class);
                    if (q != null) {
                        fullList.add(0, q); // Latest first
                    }
                }
                filter(binding.etSearch.getText() != null ? binding.etSearch.getText().toString() : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CropQualityHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
