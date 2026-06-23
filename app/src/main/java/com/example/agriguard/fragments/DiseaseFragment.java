package com.example.agriguard.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.agriguard.R;
import com.example.agriguard.databinding.FragmentDiseaseBinding;
import com.example.agriguard.models.PredictionResult;
import com.example.agriguard.utils.BitmapUtils;
import com.example.agriguard.viewmodels.DetectionViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiseaseFragment extends Fragment {

    private FragmentDiseaseBinding binding;
    private DetectionViewModel viewModel;
    private Uri currentImageUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    showPreview(currentImageUri);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    showPreview(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiseaseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DetectionViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnCapture.setOnClickListener(v -> checkCameraPermission());
        binding.btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnDetect.setOnClickListener(v -> startDetection());
        binding.btnRemoveImage.setOnClickListener(v -> resetUI());

        binding.btnTryAgain.setOnClickListener(v -> resetUI());
        binding.btnScanAnother.setOnClickListener(v -> resetUI());
        
        // Scan Another Image button text is already set to "Scan Again" in setupListeners if needed,
        // but the requirement asks for "Scan Another Image" and "Upload Another Image"
        binding.btnScanAnother.setText("Scan Another Image");
        binding.btnTryAgain.setText("Upload Another Image");
        
        binding.btnErrorCamera.setOnClickListener(v -> {
            resetUI();
            checkCameraPermission();
        });
        binding.btnErrorGallery.setOnClickListener(v -> {
            resetUI();
            galleryLauncher.launch("image/*");
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnDetect.setEnabled(!loading);
            if (loading) {
                binding.btnCapture.setEnabled(false);
                binding.btnGallery.setEnabled(false);
            } else {
                binding.btnCapture.setEnabled(true);
                binding.btnGallery.setEnabled(true);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.errorCard.setVisibility(View.VISIBLE);
                binding.resultCard.setVisibility(View.GONE);
                binding.uploadCard.setVisibility(View.VISIBLE);
                
                switch (error) {
                    case "INVALID_CROP":
                        binding.tvErrorTitle.setText("Invalid Crop Image");
                        binding.tvErrorMessage.setText("Please upload a valid crop leaf image.");
                        break;
                    default:
                        binding.tvErrorTitle.setText("Prediction Error");
                        binding.tvErrorMessage.setText(error);
                        break;
                }
            }
        });

        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if ("Invalid".equals(result.getStatus())) {
                    showError("Invalid Crop Image", result.getDescription());
                } else {
                    displayResult(result);
                }
            }
        });
    }

    private void showError(String title, String message) {
        binding.errorCard.setVisibility(View.VISIBLE);
        binding.resultCard.setVisibility(View.GONE);
        binding.uploadCard.setVisibility(View.VISIBLE);
        binding.tvErrorTitle.setText(title);
        binding.tvErrorMessage.setText(message);
    }

    private void displayResult(PredictionResult result) {
        binding.resultCard.setVisibility(View.VISIBLE);
        binding.errorCard.setVisibility(View.GONE);
        binding.uploadCard.setVisibility(View.GONE);

        boolean isHealthy = result.getStatus().equalsIgnoreCase("Healthy");
        
        binding.tvResultStatus.setText(result.getStatus());
        binding.tvResultStatus.setBackgroundResource(
                isHealthy ? R.drawable.bg_status_healthy : R.drawable.bg_status_diseased
        );

        // Requirement 2 & 3: Handle Unknown Crop and Warning Badge
        if (!result.isCropIdentified()) {
            binding.tvResultCrop.setText("Crop: Unknown Crop");
            binding.tvCropWarning.setVisibility(View.VISIBLE);
        } else {
            binding.tvResultCrop.setText("Crop: " + result.getCropName());
            binding.tvCropWarning.setVisibility(View.GONE);
        }

        binding.tvResultDisease.setText(result.getDiseaseName());
        
        if (isHealthy) {
            binding.tvResultDisease.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
        } else {
            binding.tvResultDisease.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        }
        
        binding.tvTop3.setVisibility(View.GONE);
        binding.tvResultConfidence.setVisibility(View.VISIBLE);
        binding.tvResultConfidence.setText("Confidence: " + result.getConfidenceString());

        // Requirement 1: Always display disease information
        binding.tvResultDesc.setText(result.getDescription());
        binding.tvResultSymptoms.setText(result.getSymptoms());
        binding.tvResultCauses.setText(result.getCauses());
        binding.tvResultFertilizer.setText(result.getFertilizer());
        binding.tvResultPesticides.setText(result.getPesticides());
        binding.tvResultTips.setText(result.getPreventionTips());
        binding.tvResultIrrigation.setText(result.getIrrigationTips());

        // Make sure all fields are visible
        binding.tvResultFertilizer.setVisibility(View.VISIBLE);
        binding.tvResultTips.setVisibility(View.VISIBLE);
    }

    private void startDetection() {
        if (currentImageUri == null) return;
        
        binding.resultCard.setVisibility(View.GONE);
        binding.errorCard.setVisibility(View.GONE);

        try {
            Bitmap bitmap = BitmapUtils.getCorrectlyOrientedBitmap(requireContext(), currentImageUri);
            viewModel.detect(bitmap);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPreview(Uri uri) {
        currentImageUri = uri;
        try {
            Bitmap bitmap = BitmapUtils.getCorrectlyOrientedBitmap(requireContext(), uri);
            binding.ivPreview.setImageBitmap(bitmap);
        } catch (IOException e) {
            binding.ivPreview.setImageURI(uri);
        }
        binding.ivPreview.setVisibility(View.VISIBLE);
        binding.btnRemoveImage.setVisibility(View.VISIBLE);
        binding.uploadPlaceholder.setVisibility(View.GONE);
        binding.btnDetect.setVisibility(View.VISIBLE);
        binding.resultCard.setVisibility(View.GONE);
        binding.errorCard.setVisibility(View.GONE);
    }

    private void resetUI() {
        currentImageUri = null;
        binding.ivPreview.setImageURI(null);
        binding.ivPreview.setVisibility(View.GONE);
        binding.btnRemoveImage.setVisibility(View.GONE);
        binding.uploadPlaceholder.setVisibility(View.VISIBLE);
        binding.uploadCard.setVisibility(View.VISIBLE);
        binding.btnDetect.setVisibility(View.GONE);
        binding.resultCard.setVisibility(View.GONE);
        binding.errorCard.setVisibility(View.GONE);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            currentImageUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.agriguard.fileprovider",
                    photoFile);
            takePictureLauncher.launch(currentImageUri);
        } catch (IOException ex) {
            Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
