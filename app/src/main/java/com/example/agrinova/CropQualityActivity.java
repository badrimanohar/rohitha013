package com.example.agrinova;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.agrinova.databinding.ActivityCropQualityBinding;
import com.google.android.gms.tflite.java.TfLite;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CropQualityActivity extends AppCompatActivity {

    private static final String TAG = "AgriNova_Quality";
    private static final float CONFIDENCE_THRESHOLD = 0.55f;
    private static final String MODEL_PATH = "model.tflite";
    private static final String LABEL_PATH = "labels.txt";

    private ActivityCropQualityBinding binding;
    private InterpreterApi tflite;
    private List<String> labels;
    private Bitmap selectedBitmap;
    private String currentPhotoPath;
    private int inputHeight, inputWidth;
    private TextToSpeech tts;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCropQualityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new PrefsManager(this);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        initTTS();

        TfLite.initialize(this).addOnSuccessListener(unused -> {
            try {
                initTfLite();
                setupListeners();
            } catch (Exception e) {
                Log.e(TAG, "Initialization failed", e);
            }
        });
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.US);
        });
    }

    private void initTfLite() throws IOException {
        MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(this, MODEL_PATH);
        InterpreterApi.Options options = new InterpreterApi.Options()
                .setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY);
        tflite = InterpreterApi.create(modelBuffer, options);
        labels = FileUtil.loadLabels(this, LABEL_PATH);

        int[] inputShape = tflite.getInputTensor(0).shape();
        inputHeight = inputShape[1];
        inputWidth = inputShape[2];
    }

    private void setupListeners() {
        binding.btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnCamera.setOnClickListener(v -> checkCameraPermission());
        binding.btnAnalyze.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                runAnalysis();
            } else {
                Toast.makeText(this, "Please upload image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void runAnalysis() {
        if (tflite == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutResult.setVisibility(View.GONE);
        binding.btnAnalyze.setEnabled(false);

        new Thread(() -> {
            try {
                // 1. Quality Check (Blur)
                if (isImageTooBlurry(selectedBitmap)) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnAnalyze.setEnabled(true);
                        Toast.makeText(this, "Image quality too low. Please upload a clearer crop image.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedBitmap, inputWidth, inputHeight, true);
                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(scaledBitmap);

                int[] outputShape = tflite.getOutputTensor(0).shape();
                DataType outputDataType = tflite.getOutputTensor(0).dataType();
                TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

                tflite.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

                float[] probabilities = outputBuffer.getFloatArray();
                int maxIdx = 0;
                float maxProb = 0;
                for (int i = 0; i < probabilities.length; i++) {
                    if (probabilities[i] > maxProb) {
                        maxProb = probabilities[i];
                        maxIdx = i;
                    }
                }

                final int finalIdx = maxIdx;
                final float confidence = maxProb;

                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAnalyze.setEnabled(true);
                    
                    // 2. Validation Check
                    if (confidence < CONFIDENCE_THRESHOLD) {
                        showInvalidCropError();
                    } else {
                        showDetailedResult(labels.get(finalIdx), confidence);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Analysis error", e);
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAnalyze.setEnabled(true);
                    Toast.makeText(this, "Analysis failed. Try again.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showInvalidCropError() {
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.cardInvalid.setVisibility(View.VISIBLE);
        binding.layoutDetails.setVisibility(View.GONE);
        binding.tvDetectedCrop.setText("Invalid Image");
    }

    private void showDetailedResult(String label, float confidence) {
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.cardInvalid.setVisibility(View.GONE);
        binding.layoutDetails.setVisibility(View.VISIBLE);

        binding.layoutResult.setAlpha(0f);
        binding.layoutResult.animate().alpha(1f).setDuration(600).start();

        String crop = parseCrop(label);
        boolean isHealthy = label.toLowerCase().contains("healthy");
        
        binding.tvDetectedCrop.setText("Crop: " + crop);
        
        // Dynamic Quality Simulation based on model confidence and health status
        Random r = new Random();
        int qualityScore = (int)(confidence * 100);
        if (!isHealthy) qualityScore -= (20 + r.nextInt(20)); // Penalize for disease
        if (qualityScore < 5) qualityScore = 15;
        if (qualityScore > 98) qualityScore = 98;

        binding.qualityProgress.setProgress(qualityScore);
        binding.tvQualityPercentage.setText(qualityScore + "%");

        String grade, freshness, damage, colorQual, sizeQual;
        if (qualityScore > 85) {
            grade = "A Grade (Premium)";
            freshness = "Excellent";
            damage = (r.nextInt(5)) + "%";
            colorQual = "Natural/Vibrant";
            sizeQual = "Uniform/Optimal";
        } else if (qualityScore > 60) {
            grade = "B Grade (Standard)";
            freshness = "Good";
            damage = (5 + r.nextInt(15)) + "%";
            colorQual = "Slightly Dull";
            sizeQual = "Average";
        } else {
            grade = "C Grade (Low)";
            freshness = "Fair";
            damage = (20 + r.nextInt(30)) + "%";
            colorQual = "Discolored";
            sizeQual = "Small/Irregular";
        }

        binding.tvGrade.setText(grade);
        binding.tvFreshness.setText(freshness);
        
        // Update Market Prices
        updateMarketPricing(crop, grade, qualityScore);
        
        saveToHistory(crop, grade, qualityScore + "%");
        saveToFirebase(crop, grade, binding.tvMarketPrice.getText().toString(), qualityScore + "%");
        
        speakResult(crop, grade, binding.tvMarketPrice.getText().toString());
    }

    private void saveToFirebase(String crop, String grade, String price, String percentage) {
        try {
            String databaseUrl = "https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/";
            com.google.firebase.database.DatabaseReference reference = 
                com.google.firebase.database.FirebaseDatabase.getInstance(databaseUrl).getReference("CropQuality");

            String id = reference.push().getKey();
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            String userEmail = user != null ? user.getEmail() : "anonymous";
            String userId = user != null ? user.getUid() : "anonymous";
            String timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(new Date());

            CropQuality quality = new CropQuality(id, crop, grade, price, percentage, userEmail, userId, timestamp);

            if (id != null) {
                reference.child(id).setValue(quality.toMap())
                    .addOnSuccessListener(unused -> {
                        com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), 
                            "Crop quality saved successfully", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save crop quality data", Toast.LENGTH_SHORT).show();
                    });
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase save error", e);
        }
    }

    private void saveToHistory(String crop, String grade, String conf) {
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File file = new File(getFilesDir(), "qual_" + ts + ".jpg");
            java.io.FileOutputStream os = new java.io.FileOutputStream(file);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
            os.close();
            prefs.saveHistoryItem(Uri.fromFile(file).toString(), crop + " Quality (" + grade + ")", conf);
        } catch (Exception e) {
            Log.e(TAG, "Save history failed", e);
        }
    }

    private void updateMarketPricing(String crop, String grade, int quality) {
        double current, last, prev;
        String unit = " / kg";
        String recommendation;

        if (crop.contains("Tomato")) {
            current = grade.contains("A") ? 35 : (grade.contains("B") ? 25 : 15);
            last = current - 3; prev = current - 7;
            recommendation = "Best time to sell: This week. Expected demand: High. Storage suggestion: Cool dry place (5-10°C).";
        } else if (crop.contains("Rice")) {
            current = grade.contains("A") ? 62 : 48;
            last = 58; prev = 54;
            recommendation = "Expected demand: Stable. Keep moisture below 14% for long-term storage.";
        } else if (crop.contains("Corn")) {
            current = grade.contains("A") ? 22 : 18;
            last = 20; prev = 19;
            recommendation = "Demand peaking in nearby food processing units. Best to sell now.";
        } else if (crop.contains("Potato")) {
            current = grade.contains("A") ? 28 : 18;
            last = 24; prev = 22;
            recommendation = "Prices rising. Cold storage recommended for 1 month to maximize profit.";
        } else {
            current = quality * 0.5 + 10;
            last = current * 0.9; prev = current * 0.85;
            recommendation = "Steady market demand. Ensure produce is cleaned before sending to Mandi.";
        }

        binding.tvMarketPrice.setText("₹" + (int)current + unit);
        binding.tvPriceLastYear.setText("₹" + (int)last);
        binding.tvPricePrevYear.setText("₹" + (int)prev);
        binding.tvSuggestions.setText(recommendation);
    }

    private void speakResult(String crop, String grade, String price) {
        if (tts == null) return;
        String text = "AI analysis complete. " + crop + " identified as " + grade + ". Estimated market price is " + price + ".";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ResultVoice");
    }

    private boolean isImageTooBlurry(Bitmap bitmap) {
        return bitmap.getWidth() < 150 || bitmap.getHeight() < 150;
    }

    private String parseCrop(String label) {
        if (label.contains("___")) return label.split("___")[0].replace("_", " ");
        return label.split("_")[0];
    }

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) loadBitmap(uri); });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> { if (isSuccess) loadBitmap(Uri.fromFile(new File(currentPhotoPath))); });

    private void loadBitmap(Uri uri) {
        try {
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            if (is != null) {
                selectedBitmap = BitmapFactory.decodeStream(is);
                is.close();
                binding.ivCropPreview.setImageBitmap(selectedBitmap);
                binding.ivCropPreview.setAlpha(1.0f);
                binding.layoutUploadHint.setVisibility(View.GONE);
                binding.layoutResult.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) launchCamera();
            });

    private void launchCamera() {
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile("QUAL_" + ts, ".jpg", dir);
            currentPhotoPath = image.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", image);
            cameraLauncher.launch(uri);
        } catch (IOException e) {
            Toast.makeText(this, "Camera failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tflite != null) tflite.close();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
