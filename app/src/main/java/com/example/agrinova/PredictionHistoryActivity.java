package com.example.agrinova;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agrinova.databinding.ActivityPredictionHistoryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PredictionHistoryActivity extends AppCompatActivity {

    private ActivityPredictionHistoryBinding binding;
    private PredictionViewModel viewModel;
    private PredictionAdapter adapter;
    private List<PredictionModel> allPredictions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPredictionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(PredictionViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupSearch();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            // Data is live in Firebase, but we can re-trigger if needed
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.fabExport.setOnClickListener(v -> {
            exportToPDF();
        });
    }

    private void setupRecyclerView() {
        adapter = new PredictionAdapter(new ArrayList<>());
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getPredictions().observe(this, list -> {
            if (list != null) {
                allPredictions = list;
                filter(binding.etSearch.getText().toString());
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        List<PredictionModel> filtered = new ArrayList<>();
        for (PredictionModel pm : allPredictions) {
            if (pm.getCommodity().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(pm);
            }
        }
        adapter.setPredictions(filtered);
    }

    private void exportToPDF() {
        if (allPredictions.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create();
        android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);

        android.graphics.Canvas canvas = page.getCanvas();
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setTextSize(12f);

        canvas.drawText("AgriNova - Market Price Prediction History", 50, 50, paint);
        
        int y = 100;
        for (int i = 0; i < Math.min(allPredictions.size(), 20); i++) {
            PredictionModel pm = allPredictions.get(i);
            String text = String.format(Locale.getDefault(), "%s: ₹ %.2f (%s)", pm.getCommodity(), pm.getPredictedPrice(), pm.getPredictionDate());
            canvas.drawText(text, 50, y, paint);
            y += 30;
        }

        document.finishPage(page);

        java.io.File file = new java.io.File(getExternalFilesDir(null), "PredictionHistory.pdf");
        try {
            document.writeTo(new java.io.FileOutputStream(file));
            Toast.makeText(this, "PDF saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (java.io.IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }
}
