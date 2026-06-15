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

import com.example.agrinova.databinding.ActivityDiseaseDetectionBinding;
import com.google.android.gms.tflite.java.TfLite;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class DiseaseDetectionActivity extends AppCompatActivity {

    private static final String TAG = "AgriNova_Detection";
    private static final float CROP_CONFIDENCE_THRESHOLD = 0.30f;
    private static final String MODEL_PATH = "rice_disease_model.tflite";
    private static final String[] RICE_LABELS = {
            "Bacterial leaf blight",
            "Brown spot",
            "Leaf smut"
    };

    private ActivityDiseaseDetectionBinding binding;
    private InterpreterApi tflite;
    private Bitmap selectedBitmap;
    private String currentPhotoPath;
    private PrefsManager prefs;
    private TextToSpeech tts;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    private int inputHeight, inputWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityDiseaseDetectionBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            prefs = new PrefsManager(this);
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance();

            if (binding.toolbar != null) {
                binding.toolbar.setNavigationOnClickListener(v -> finish());
            }

            initTTS();

            TfLite.initialize(this).addOnSuccessListener(unused -> {
                try {
                    initTfLite();
                    setupListeners();
                } catch (Exception e) {
                    Log.e(TAG, "Initialization failed", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Activity initialization failed", e);
            finish();
        }
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (tts != null) tts.setLanguage(Locale.US);
            }
        });
    }

    private void initTfLite() throws IOException {
        MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(this, MODEL_PATH);
        InterpreterApi.Options options = new InterpreterApi.Options()
                .setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY);
        tflite = InterpreterApi.create(modelBuffer, options);

        // Required input size: 224x224
        inputHeight = 224;
        inputWidth = 224;
    }

    private void setupListeners() {
        if (binding.btnGallery != null) binding.btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        if (binding.btnCamera != null) binding.btnCamera.setOnClickListener(v -> checkCameraPermission());
        if (binding.btnDetect != null) {
            binding.btnDetect.setOnClickListener(v -> {
                if (selectedBitmap == null) {
                    Toast.makeText(this, getString(R.string.error_select_image), Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (selectedBitmap.getWidth() < 100 || selectedBitmap.getHeight() < 100) {
                    Toast.makeText(this, getString(R.string.error_image_small), Toast.LENGTH_SHORT).show();
                    return;
                }

                runInference();
            });
        }

        if (binding.btnRetry != null) {
            binding.btnRetry.setOnClickListener(v -> {
                binding.cardResult.setVisibility(View.GONE);
                binding.layoutUploadHint.setVisibility(View.VISIBLE);
                binding.ivCropPreview.setAlpha(0.1f);
                binding.ivCropPreview.setImageResource(R.drawable.ic_logo);
                selectedBitmap = null;
            });
        }
    }

    private void runInference() {
        if (tflite == null) {
            Toast.makeText(this, getString(R.string.model_not_ready), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnDetect.setEnabled(false);
        binding.cardResult.setVisibility(View.GONE);
        binding.tvValidationStatus.setVisibility(View.VISIBLE);
        binding.tvValidationStatus.setText(R.string.analyzing_image);
        binding.tvValidationStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        new Thread(() -> {
            try {
                // 1. Strict Quality Validations
                boolean isBlurry = isImageTooBlurry(selectedBitmap);
                boolean isDark = isImageTooDark(selectedBitmap);
                boolean isNatural = hasNaturalFeatures(selectedBitmap);

                if (isBlurry) {
                    runOnUiThread(() -> {
                        updateValidationStatus(false);
                        showInvalidImageError(getString(R.string.blurry_image_detected), getString(R.string.blurry_image_desc));
                    });
                    return;
                }

                if (isDark) {
                    runOnUiThread(() -> {
                        updateValidationStatus(false);
                        showInvalidImageError(getString(R.string.invalid_crop_image), getString(R.string.error_dark_image));
                    });
                    return;
                }

                if (!isNatural) {
                    runOnUiThread(() -> {
                        updateValidationStatus(false);
                        showInvalidImageError(getString(R.string.invalid_crop_image), getString(R.string.invalid_image_detected));
                    });
                    return;
                }

                // 2. AI validation and Prediction
                ImageProcessor imageProcessor = new ImageProcessor.Builder()
                        .add(new ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                        .add(new NormalizeOp(0.0f, 255.0f)) // Normalize to [0, 1]
                        .build();

                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(selectedBitmap);
                tensorImage = imageProcessor.process(tensorImage);

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
                final float finalProb = maxProb;

                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnDetect.setEnabled(true);
                    
                    if (finalProb < CROP_CONFIDENCE_THRESHOLD) {
                        updateValidationStatus(false);
                        showInvalidImageError(getString(R.string.invalid_crop_image), getString(R.string.invalid_image_detected));
                    } else {
                        updateValidationStatus(true);
                        showResult(finalIdx, finalProb);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Detection failed", e);
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnDetect.setEnabled(true);
                    Toast.makeText(this, getString(R.string.prediction_failure), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateValidationStatus(boolean isValid) {
        binding.tvValidationStatus.setVisibility(View.VISIBLE);
        if (isValid) {
            binding.tvValidationStatus.setText(R.string.validation_status_valid);
            binding.tvValidationStatus.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
        } else {
            binding.tvValidationStatus.setText(R.string.validation_status_invalid);
            binding.tvValidationStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    private boolean isImageTooDark(Bitmap bitmap) {
        if (bitmap == null) return true;
        
        long sum = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int sampleSize = 10;
        
        for (int y = 0; y < height; y += sampleSize) {
            for (int x = 0; x < width; x += sampleSize) {
                int color = bitmap.getPixel(x, y);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                sum += (r + g + b) / 3;
            }
        }
        
        float avgBrightness = (float) sum / ((width / (float)sampleSize) * (height / (float)sampleSize));
        return avgBrightness < 45; // Threshold for dark image
    }

    private boolean isImageTooBlurry(Bitmap bitmap) {
        if (bitmap == null) return true;
        if (bitmap.getWidth() < 200 || bitmap.getHeight() < 200) return true;
        
        // Simple variance check for blur detection
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int sampleSize = 4;
        long sum = 0;
        long sumSq = 0;
        int count = 0;
        
        for (int y = sampleSize; y < height - sampleSize; y += sampleSize) {
            for (int x = sampleSize; x < width - sampleSize; x += sampleSize) {
                int p1 = bitmap.getPixel(x, y) & 0xFF;
                int p2 = bitmap.getPixel(x + 1, y) & 0xFF;
                int diff = Math.abs(p1 - p2);
                sum += diff;
                sumSq += (long) diff * diff;
                count++;
            }
        }
        
        if (count == 0) return true;
        double variance = (double) sumSq / count - (double) (sum * sum) / (count * count);
        return variance < 50; // threshold for blur
    }

    private boolean hasNaturalFeatures(Bitmap bitmap) {
        if (bitmap == null) return false;
        // Check for "greenish" content as a proxy for plants
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int greenCount = 0;
        int sampleSize = 10;
        int totalSamples = 0;
        
        for (int y = 0; y < height; y += sampleSize) {
            for (int x = 0; x < width; x += sampleSize) {
                int color = bitmap.getPixel(x, y);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                if (g > r && g > b && g > 40) greenCount++;
                totalSamples++;
            }
        }
        // At least 20% should be greenish for plant images
        return (float) greenCount / totalSamples > 0.20f;
    }

    private void showInvalidImageError(String title, String desc) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnDetect.setEnabled(true);
        binding.cardResult.setVisibility(View.VISIBLE);
        binding.layoutSuccess.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.cardResult.setStrokeColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        binding.tvErrorTitle.setText(title);
        binding.tvErrorDesc.setText(desc);
        
        AnimUtils.shake(binding.cardResult);
    }

    private void showResult(int index, float confidence) {
        binding.cardResult.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutSuccess.setVisibility(View.VISIBLE);
        binding.cardResult.setStrokeColor(ContextCompat.getColor(this, R.color.primary_green));
        
        binding.cardResult.setAlpha(0f);
        binding.cardResult.animate().alpha(1f).setDuration(500).start();

        String disease = index < RICE_LABELS.length ? RICE_LABELS[index] : "Unknown";
        String crop = "Rice";
        boolean isHealthy = false; // The model has 3 disease labels

        binding.tvDiseaseName.setText(disease);
        String confPercent = (int)(confidence * 100) + "%";
        binding.tvConfidence.setText(getString(R.string.ai_confidence, confPercent));
        binding.tvSuggestion.setText(getString(R.string.detected_crop, crop));

        int severity = (int)(confidence * 100);
        binding.progressSeverity.setProgress(severity);
        binding.progressSeverity.setProgressTintList(android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(this, (severity > 80 ? android.R.color.holo_red_dark : android.R.color.holo_orange_dark))));

        updateDetailedInfo(disease, isHealthy);

        binding.btnVoice.setOnClickListener(v -> speakResult(isHealthy ? "Your " + crop + " crop is healthy." : "Detected " + disease + " on " + crop + ". Suggested treatment: " + binding.tvTreatment.getText()));

        uploadToFirebase(crop, isHealthy ? "Healthy" : disease, confPercent);

        saveToHistory(crop, isHealthy ? "Healthy" : disease, confPercent);
        
        binding.btnShareCommunity.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, ChatActivity.class);
            intent.putExtra("communityId", "default");
            String sharedText = getString(R.string.need_help_community, (isHealthy ? "Healthy" : disease), crop, (isHealthy ? "" : getString(R.string.community_suggestion)));
            intent.putExtra("sharedText", sharedText);
            startActivity(intent);
        });
    }

    private void uploadToFirebase(String cropName, String diseaseName, String conf) {
        if (cropName == null || diseaseName == null || selectedBitmap == null) {
            Log.e(TAG, "Cannot save null detection data");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnDetect.setEnabled(false);

        // 1. Upload Image to Firebase Storage first
        String fileName = "detections/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        imageRef.putBytes(data)
            .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveDataToDatabase(cropName, diseaseName, conf, uri.toString());
            }))
            .addOnFailureListener(e -> {
                Log.e(TAG, "Storage upload failed, saving without image", e);
                saveDataToDatabase(cropName, diseaseName, conf, null);
            });
    }

    private void saveDataToDatabase(String cropName, String diseaseName, String conf, String imageUrl) {
        String treatment = binding.tvTreatment.getText().toString();
        String fertilizer = binding.tvFertilizer.getText().toString();
        String prevention = binding.tvPrevention.getText().toString();
        
        if (treatment.isEmpty()) treatment = "No specific treatment info";
        if (fertilizer.isEmpty()) fertilizer = "Balanced organic fertilizer";

        try {
            String databaseUrl = "https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/";
            com.google.firebase.database.DatabaseReference reference =
                    com.google.firebase.database.FirebaseDatabase.getInstance(databaseUrl).getReference("CropDetections");

            String id = reference.push().getKey();

            java.util.HashMap<String, Object> map = new java.util.HashMap<>();
            map.put("cropName", cropName);
            map.put("diseaseName", diseaseName);
            map.put("confidence", conf);
            map.put("treatment", treatment);
            map.put("fertilizer", fertilizer);
            map.put("prevention", prevention);
            map.put("imageUrl", imageUrl);
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));
            map.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                map.put("userEmail", user.getEmail());
                map.put("userId", user.getUid());
            }

            if (id != null) {
                reference.child(id).setValue(map)
                        .addOnSuccessListener(unused -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnDetect.setEnabled(true);
                            Toast.makeText(DiseaseDetectionActivity.this, "Detection Record Saved", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnDetect.setEnabled(true);
                            Toast.makeText(DiseaseDetectionActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnDetect.setEnabled(true);
            Log.e(TAG, "Firebase save failed", e);
        }
    }

    private void updateDetailedInfo(String label, boolean isHealthy) {
        String description;
        String causes;
        String prevention;
        String treatment;
        String fertilizer;
        String pesticide;

        if (isHealthy) {
            description = "The crop shows no signs of disease or pest infestation. The foliage is vibrant and growth is steady.";
            causes = "Optimal growth environment, timely irrigation, and proper nutrient management.";
            prevention = "Continue regular monitoring and soil testing. Maintain crop rotation.";
            treatment = "Maintain current irrigation and organic care. No chemical intervention needed.";
            fertilizer = "Balanced compost or well-rotted manure (1 kg per square meter).";
            pesticide = "No pesticides needed. Encourage beneficial insects like ladybugs.";
        } else {
            if (label.contains("Blight")) {
                description = "Blight is a rapid and complete chlorosis, browning, then death of plant tissues such as leaves, branches, twigs, or floral organs.";
                causes = "Fungal/Bacterial pathogens spread by water splash, wind, or contaminated tools.";
                prevention = "Avoid overhead watering. Space plants for airflow. Remove crop debris after harvest.";
                treatment = "Remove and burn infected leaves immediately. Apply copper-based fungicide sprays.";
                fertilizer = "Apply low nitrogen, high potassium fertilizer (e.g., NPK 5-10-20) to boost immunity.";
                pesticide = "Organic: Neem oil. Chemical: Chlorothalonil or Mancozeb sprays.";
            } else if (label.contains("Rust")) {
                description = "Rust diseases are caused by fungal pathogens. They appear as yellow, orange, red, or brown pustules on the undersides of leaves.";
                causes = "Puccinia fungus thriving in high humidity, heavy dew, and frequent rain.";
                prevention = "Use rust-resistant crop varieties. Destroy host weeds around the field.";
                treatment = "Increase sunlight exposure. Prune heavily infected branches to improve ventilation.";
                fertilizer = "Potassium-rich fertilizers (e.g., Potash) to strengthen plant cell walls.";
                pesticide = "Apply sulfur-based fungicides or systemic fungicides like Propiconazole.";
            } else if (label.contains("Spot")) {
                description = "Leaf spots are roundish blemishes found on the leaves of many species of plants, mostly caused by parasitic fungi or bacteria.";
                causes = "Xanthomonas or Alternaria pathogens. Spreads rapidly in warm, moist weather.";
                prevention = "Rotate crops every 3 years. Sanitize all tools with 10% bleach solution.";
                treatment = "Pruning for better ventilation. Mulch around the base to prevent soil splash.";
                fertilizer = "Apply organic seaweed extract or balanced liquid fertilizer (NPK 10-10-10).";
                pesticide = "Fixed copper sprays or Bordeaux mixture. Organic: Potassium bicarbonate.";
            } else if (label.contains("smut") || label.contains("Smut")) {
                description = "Smut is a fungal disease that affects many cereal crops, including rice. It typically results in black, powdery masses of spores replacing the grain.";
                causes = "Tilletia or Ustilago fungi. Often soil-borne or seed-borne.";
                prevention = "Use certified clean seeds. Practice deep plowing to bury spores.";
                treatment = "Remove and destroy infected panicles. Avoid excessive nitrogen application.";
                fertilizer = "Apply balanced NPK with emphasis on Silicon to strengthen stems.";
                pesticide = "Seed treatment with Carboxin or Thiram. Foliar spray of Propiconazole if detected early.";
            } else if (label.contains("Spider_mites")) {
                description = "Spider mites are tiny arachnids that suck the sap from leaves, causing yellow speckling and fine webbing on the plant.";
                causes = "Hot, dry conditions and dusty environments. Lack of natural predators.";
                prevention = "Keep plants well-hydrated. Use windbreaks to reduce dust. Avoid broad-spectrum insecticides.";
                treatment = "Blast the underside of leaves with a strong water spray once a week.";
                fertilizer = "Maintain consistent moisture; avoid high-nitrogen fertilizers which attract mites.";
                pesticide = "Organic: Insecticidal soap or Neem oil. Chemical: Abamectin or Spiromesifen.";
            } else {
                description = "General plant pathogen attack causing physiological stress and reducing yield potential.";
                causes = "Pathogen attack (Fungi/Bacteria/Virus) often triggered by environmental stress.";
                prevention = "Use certified disease-free seeds. Maintain proper drainage and soil pH.";
                treatment = "Isolate infected plants. Optimize nutrient supply to help plant recovery.";
                fertilizer = "Slow-release balanced fertilizer (e.g., NPK 15-15-15) for overall vigor.";
                pesticide = "Consult local agricultural extension for specific regional recommendations.";
            }
        }

        binding.tvDescription.setText(description);
        binding.tvCauses.setText(causes);
        binding.tvPrevention.setText(prevention);
        binding.tvTreatment.setText(treatment);
        binding.tvFertilizer.setText(fertilizer);
        binding.tvPesticide.setText(pesticide);
    }

    private void speakResult(String message) {
        if (tts == null) return;
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "Speech");
    }

    private String saveToHistory(String crop, String disease, String conf) {
        try {
            // Requirement: Do NOT store images. Skipping local image save.
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            prefs.saveHistoryItem(null, crop + " (" + disease + ")", conf);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Save history failed", e);
            return null;
        }
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
                binding.cardResult.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.image_load_failed), Toast.LENGTH_SHORT).show();
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
            File image = File.createTempFile("CROP_" + ts, ".jpg", dir);
            currentPhotoPath = image.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", image);
            cameraLauncher.launch(uri);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.camera_failed), Toast.LENGTH_SHORT).show();
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
