package com.example.agrinova;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import com.example.agrinova.databinding.ActivityDiseaseDetectionBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiseaseDetectionActivity extends AppCompatActivity {

    private static final String TAG = "DiseaseDetection";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String DATABASE_URL = "https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private ActivityDiseaseDetectionBinding binding;
    private TFLiteHelper tfliteHelper;
    private Uri currentPhotoUri;
    private String currentPhotoPath;
    private String lastDetectedDisease = "None";
    private String lastDetectedCrop = "Unknown";

    // Modern Activity Result Launchers
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processImage(uri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && currentPhotoUri != null) {
                    processImage(currentPhotoUri);
                } else {
                    Log.d(TAG, "Camera capture cancelled or failed");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityDiseaseDetectionBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            binding.toolbar.setNavigationOnClickListener(v -> finish());

            initModel();
            setupClickListeners();

            if (savedInstanceState != null) {
                currentPhotoPath = savedInstanceState.getString("photo_path");
                if (currentPhotoPath != null) {
                    currentPhotoUri = Uri.fromFile(new File(currentPhotoPath));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Initialization failed", e);
            finish();
        }
    }

    private void initModel() {
        try {
            tfliteHelper = new TFLiteHelper(this);
            boolean isInitialized = tfliteHelper.init("crop_disease_model.tflite", "labels.txt");
            if (!isInitialized) {
                Toast.makeText(this, "Failed to load detection model", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Model initialization failed", e);
        }
    }

    private void setupClickListeners() {
        binding.btnCamera.setOnClickListener(v -> checkPermissionsAndLaunch(true));
        binding.btnGallery.setOnClickListener(v -> checkPermissionsAndLaunch(false));
    }

    private void checkPermissionsAndLaunch(boolean isCamera) {
        String[] permissions;
        if (isCamera) {
            permissions = new String[]{Manifest.permission.CAMERA};
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
            } else {
                permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            }
        }

        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            if (isCamera) launchCamera(); else galleryLauncher.launch("image/*");
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    launchCamera();
                } else {
                    galleryLauncher.launch("image/*");
                }
            } else {
                Toast.makeText(this, "Permission required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                cameraLauncher.launch(currentPhotoUri);
            }
        } catch (IOException ex) {
            Log.e(TAG, "Camera launch failed", ex);
            Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("DET_" + timeStamp, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void processImage(Uri imageUri) {
        if (imageUri == null) return;
        
        binding.loadingProgress.setVisibility(View.VISIBLE);
        binding.tvPlaceholder.setVisibility(View.GONE);
        binding.layoutResults.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Bitmap bitmap = loadResizedBitmap(imageUri);
                if (bitmap == null) {
                    runOnUiThread(() -> {
                        binding.loadingProgress.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                List<TFLiteHelper.Recognition> results = tfliteHelper.runInference(bitmap);

                runOnUiThread(() -> {
                    binding.loadingProgress.setVisibility(View.GONE);
                    binding.ivPreview.setAlpha(1.0f);
                    binding.ivPreview.setImageBitmap(bitmap);
                    
                    if (results != null && !results.isEmpty()) {
                        displayResults(results);
                    } else {
                        Toast.makeText(this, "No disease detected. Try a clearer photo.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Processing error", e);
                runOnUiThread(() -> {
                    binding.loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private Bitmap loadResizedBitmap(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        if (inputStream != null) inputStream.close();

        int targetSize = 1024;
        int scaleFactor = Math.min(options.outWidth / targetSize, options.outHeight / targetSize);

        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.max(1, scaleFactor);
        
        inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        if (inputStream != null) inputStream.close();
        return bitmap;
    }

    private void displayResults(List<TFLiteHelper.Recognition> results) {
        TFLiteHelper.Recognition top = results.get(0);
        lastDetectedCrop = top.getCropName();
        lastDetectedDisease = top.getDiseaseName();
        
        binding.layoutResults.setVisibility(View.VISIBLE);
        binding.tvCropName.setText("Crop: " + lastDetectedCrop);
        binding.tvDiseaseName.setText("Disease: " + lastDetectedDisease);
        float confidenceValue = top.getConfidence() * 100;
        binding.tvConfidence.setText(String.format(Locale.getDefault(), "Confidence: %.1f%%", confidenceValue));

        if (results.size() > 1) {
            StringBuilder others = new StringBuilder("Other possibilities: ");
            for (int i = 1; i < results.size(); i++) {
                others.append(results.get(i).getDiseaseName()).append(" (")
                      .append(String.format(Locale.getDefault(), "%.1f%%", results.get(i).getConfidence() * 100))
                      .append(")");
                if (i < results.size() - 1) others.append(", ");
            }
            binding.tvTopPredictions.setText(others.toString());
        } else {
            binding.tvTopPredictions.setText("");
        }

        updateRecommendations(top.getTitle());
        saveToFirebase(lastDetectedCrop, lastDetectedDisease, String.format(Locale.getDefault(), "%.0f%%", confidenceValue));
    }

    private void updateRecommendations(String label) {
        Map<String, String[]> recommendations = new HashMap<>();
        recommendations.put("Apple___Apple_scab", new String[]{
            "Apply fungicides like Captan or Mancozeb. Rake and destroy fallen leaves.",
            "Neem oil spray can be effective in early stages.",
            "Water at the base of the tree to keep leaves dry. Plant resistant varieties."
        });
        // ... (rest of recommendations)
        
        String[] advice = recommendations.get(label);
        String treatment, organic, prevention;
        if (advice == null) {
            if (label.toLowerCase().contains("healthy")) {
                treatment = "Your crop looks healthy! Continue regular maintenance.";
                organic = "Use organic compost for better yield.";
                prevention = "Keep monitoring for any signs of pests or changes.";
            } else {
                treatment = "Consult a local agricultural expert. Generic antifungal spray might help.";
                organic = "Neem oil is a broad-spectrum organic solution.";
                prevention = "Ensure proper drainage and avoid overcrowding plants.";
            }
        } else {
            treatment = advice[0];
            organic = advice[1];
            prevention = advice[2];
        }
        
        binding.tvTreatment.setText(treatment);
        binding.tvOrganic.setText(organic);
        binding.tvPrevention.setText(prevention);
    }

    private void saveToFirebase(String crop, String disease, String percentage) {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("CropQuality");
            String id = ref.push().getKey();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

            CropQuality qualityData = new CropQuality(
                id,
                crop,
                disease.contains("Healthy") ? "A Grade" : "B Grade", // Dynamic grade based on health
                "N/A", // Price not calculated here
                percentage,
                disease,
                binding.tvTreatment.getText().toString(),
                user.getEmail(),
                user.getUid(),
                timestamp
            );

            if (id != null) {
                ref.child(id).setValue(qualityData.toMap())
                    .addOnFailureListener(e -> Log.e(TAG, "Firebase save failed", e));
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase operation failed", e);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", currentPhotoPath);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tfliteHelper != null) {
            tfliteHelper.close();
        }
    }
}
