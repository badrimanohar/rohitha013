package com.example.agriguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.agriguard.databinding.FragmentPricesBinding;
import com.example.agriguard.utils.LocationData;
import com.example.agriguard.viewmodels.PriceViewModel;
import java.util.List;
import java.util.Locale;

public class PricesFragment extends Fragment {

    private FragmentPricesBinding binding;
    private PriceViewModel viewModel;

    private String selectedState, selectedDistrict, selectedMarket, selectedCrop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPricesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PriceViewModel.class);

        setupSpinners();
        observeViewModel();

        binding.btnPredict.setOnClickListener(v -> handlePrediction());
    }

    private void setupSpinners() {
        // Load all Indian States
        List<String> states = LocationData.getStates();
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, states);
        binding.spinnerState.setAdapter(stateAdapter);

        binding.spinnerState.setOnItemClickListener((parent, view, position, id) -> {
            selectedState = (String) parent.getItemAtPosition(position);
            loadDistricts(selectedState);
        });

        // Load All Indian Crops
        List<String> crops = LocationData.getAllCrops();
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, crops);
        binding.spinnerCrop.setAdapter(cropAdapter);
        binding.spinnerCrop.setOnItemClickListener((parent, view, position, id) -> selectedCrop = (String) parent.getItemAtPosition(position));
    }

    private void loadDistricts(String state) {
        List<String> districts = LocationData.getDistricts(state);
        
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, districts);
        binding.spinnerDistrict.setAdapter(districtAdapter);
        binding.spinnerDistrict.setText("", false);
        selectedDistrict = null;
        selectedMarket = null;
        binding.spinnerMarket.setText("", false);
        
        binding.spinnerDistrict.setOnItemClickListener((parent, view, position, id) -> {
            selectedDistrict = (String) parent.getItemAtPosition(position);
            loadMarkets(selectedDistrict);
        });
    }

    private void loadMarkets(String district) {
        List<String> markets = LocationData.getMarkets(district);
        ArrayAdapter<String> marketAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, markets);
        binding.spinnerMarket.setAdapter(marketAdapter);
        binding.spinnerMarket.setText("", false);
        selectedMarket = null;
        binding.spinnerMarket.setOnItemClickListener((parent, view, position, id) -> selectedMarket = (String) parent.getItemAtPosition(position));
    }

    private void handlePrediction() {
        if (selectedState == null || selectedDistrict == null || selectedMarket == null || selectedCrop == null) {
            Toast.makeText(getContext(), "Please select all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPredict.setEnabled(false);
        binding.btnPredict.setText("Predicting...");
        viewModel.predictPrice(selectedState, selectedDistrict, selectedMarket, selectedCrop);
    }

    private void observeViewModel() {
        viewModel.predictionResult.observe(getViewLifecycleOwner(), result -> {
            binding.btnPredict.setEnabled(true);
            binding.btnPredict.setText("Predict Price");
            
            binding.resultCard.setVisibility(View.VISIBLE);
            binding.tvPredictedPrice.setText(String.format(Locale.getDefault(), "₹ %.2f / Quintal", result.price));
            binding.tvConfidence.setText(String.format(Locale.getDefault(), "%.0f%%", result.confidence * 100));
            binding.tvTrend.setText(result.trend);
            binding.tvRecommendation.setText(result.recommendation);
            
            binding.resultCard.requestFocus();
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            binding.btnPredict.setEnabled(true);
            binding.btnPredict.setText("Predict Price");
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
