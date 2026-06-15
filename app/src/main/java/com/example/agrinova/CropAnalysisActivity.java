package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.agrinova.databinding.ActivityCropAnalysisBinding;
import java.util.List;
import java.util.stream.Collectors;

public class CropAnalysisActivity extends AppCompatActivity {

    private ActivityCropAnalysisBinding binding;
    private CropAdapter adapter;
    private List<Crop> allCrops;
    private String currentCategory = "All";
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCropAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        allCrops = CropDataManager.getAllCrops();
        setupRecyclerView();
        setupSearch();
        setupFilters();
    }

    private void setupRecyclerView() {
        adapter = new CropAdapter(allCrops, crop -> {
            Intent intent = new Intent(this, CropDetailActivity.class);
            intent.putExtra("selected_crop", crop);
            startActivity(intent);
        });
        binding.rvCrops.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvCrops.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearchCrop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                currentQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        findViewById(R.id.chip_all).setOnClickListener(v -> { currentCategory = "All"; applyFilters(); });
        findViewById(R.id.chip_grains).setOnClickListener(v -> { currentCategory = "Grains"; applyFilters(); });
        findViewById(R.id.chip_vegetables).setOnClickListener(v -> { currentCategory = "Vegetables"; applyFilters(); });
        findViewById(R.id.chip_fruits).setOnClickListener(v -> { currentCategory = "Fruits"; applyFilters(); });
        findViewById(R.id.chip_cash_crops).setOnClickListener(v -> { currentCategory = "Cash Crops"; applyFilters(); });
        findViewById(R.id.chip_pulses).setOnClickListener(v -> { currentCategory = "Pulses"; applyFilters(); });
    }

    private void applyFilters() {
        List<Crop> filtered = allCrops.stream().filter(crop -> {
            boolean matchesCategory = currentCategory.equals("All") || crop.category.equals(currentCategory);
            boolean matchesSearch = crop.name.toLowerCase().contains(currentQuery);
            return matchesCategory && matchesSearch;
        }).collect(Collectors.toList());
        
        adapter.updateList(filtered);
        
        if (filtered.isEmpty()) {
            Toast.makeText(this, "No crops found for your search.", Toast.LENGTH_SHORT).show();
        }
    }
}
