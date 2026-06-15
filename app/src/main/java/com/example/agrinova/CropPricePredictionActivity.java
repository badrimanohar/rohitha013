package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.agrinova.databinding.ActivityCropPricePredictionBinding;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CropPricePredictionActivity extends AppCompatActivity {

    private ActivityCropPricePredictionBinding binding;
    private PredictionViewModel viewModel;
    private JSONObject hierarchyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCropPricePredictionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(PredictionViewModel.class);

        loadHierarchyData();
        setupSpinners();
        setupObservers();
        setupActions();
    }

    private void loadHierarchyData() {
        try {
            InputStream is = getAssets().open("crop_hierarchy.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            hierarchyData = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSpinners() {
        // Load States
        List<String> states = new ArrayList<>();
        if (hierarchyData != null) {
            Iterator<String> keys = hierarchyData.keys();
            while (keys.hasNext()) states.add(keys.next());
            Collections.sort(states);
        }
        setupAutoComplete(binding.spinnerState, states);

        // Load Commodities from mapping
        Map<String, Map<String, Integer>> mappings = viewModel.getPricePredictor().getMappings();
        setupAutoComplete(binding.spinnerCommodity, getListFromMap(mappings.get("commodities")));
        
        // Setup Variety
        setupAutoComplete(binding.spinnerVariety, java.util.Arrays.asList("Common", "Hybrid"));

        // Cascading Logic
        binding.spinnerState.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = (String) parent.getItemAtPosition(position);
            updateDistricts(selectedState);
        });

        binding.spinnerDistrict.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDistrict = (String) parent.getItemAtPosition(position);
            updateMarkets(binding.spinnerState.getText().toString(), selectedDistrict);
        });
    }

    private void updateDistricts(String state) {
        binding.spinnerDistrict.setText("");
        binding.spinnerMarket.setText("");
        
        List<String> districts = new ArrayList<>();
        try {
            if (hierarchyData != null && hierarchyData.has(state)) {
                JSONObject stateObj = hierarchyData.getJSONObject(state);
                Iterator<String> keys = stateObj.keys();
                while (keys.hasNext()) districts.add(keys.next());
                Collections.sort(districts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setupAutoComplete(binding.spinnerDistrict, districts);
    }

    private void updateMarkets(String state, String district) {
        binding.spinnerMarket.setText("");
        
        List<String> markets = new ArrayList<>();
        try {
            if (hierarchyData != null && hierarchyData.has(state)) {
                JSONObject stateObj = hierarchyData.getJSONObject(state);
                if (stateObj.has(district)) {
                    org.json.JSONArray marketArray = stateObj.getJSONArray(district);
                    for (int i = 0; i < marketArray.length(); i++) {
                        markets.add(marketArray.getString(i));
                    }
                    Collections.sort(markets);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setupAutoComplete(binding.spinnerMarket, markets);
    }

    private void setupAutoComplete(android.widget.AutoCompleteTextView view, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, data);
        view.setAdapter(adapter);
        view.setDropDownBackgroundResource(android.R.color.white); // Ensure white background
    }

    private List<String> getListFromMap(Map<String, Integer> map) {
        if (map == null) return new ArrayList<>();
        List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        return list;
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnPredict.setEnabled(!isLoading);
            if (isLoading) {
                binding.btnPredict.setText("Predicting...");
            } else {
                binding.btnPredict.setText("Predict Market Price");
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.getPredictions().observe(this, list -> {
            if (list != null && !list.isEmpty()) {
                showResult(list.get(0));
            }
        });
    }

    private void showResult(PredictionModel prediction) {
        binding.cardResult.setVisibility(View.VISIBLE);
        binding.tvPredictedPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", prediction.getPredictedPrice()));
        
        String details = String.format("%s (%s)\n%s, %s", 
                prediction.getCommodity(), 
                prediction.getVariety(), 
                prediction.getMarket(), 
                prediction.getState());
        binding.tvDetails.setText(details);
        binding.tvPredictionDate.setText("Prediction Date: " + prediction.getPredictionDate());
        
        binding.cardResult.setAlpha(0f);
        binding.cardResult.animate().alpha(1f).setDuration(500).start();
        
        // Scroll to result
        binding.cardResult.post(() -> {
            binding.cardResult.getParent().requestChildFocus(binding.cardResult, binding.cardResult);
        });
    }

    private void setupActions() {
        binding.btnPredict.setOnClickListener(v -> {
            String state = binding.spinnerState.getText().toString();
            String district = binding.spinnerDistrict.getText().toString();
            String market = binding.spinnerMarket.getText().toString();
            String commodity = binding.spinnerCommodity.getText().toString();
            String variety = binding.spinnerVariety.getText().toString();

            if (state.isEmpty() || district.isEmpty() || market.isEmpty() || commodity.isEmpty() || variety.isEmpty()) {
                Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            viewModel.predictAndSave(state, district, market, commodity, variety, year, month, day);
        });

        binding.btnClear.setOnClickListener(v -> {
            binding.spinnerState.setText("");
            binding.spinnerDistrict.setText("");
            binding.spinnerMarket.setText("");
            binding.spinnerCommodity.setText("");
            binding.spinnerVariety.setText("Common");
            binding.cardResult.setVisibility(View.GONE);
        });

        binding.btnViewHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, PredictionHistoryActivity.class));
        });
    }
}
