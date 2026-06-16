package com.example.agrinova;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
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

import com.example.agrinova.databinding.ActivityCropQualityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Full working Crop Quality and Disease Detection Activity.
 * Linked to Firebase and includes Camera/Gallery fix.
 */
public class CropQualityActivity extends AppCompatActivity {

    private static final String TAG = "AgriNova_Quality";
    private static final float CONFIDENCE_THRESHOLD = 0.50f;
    private static final String MODEL_PATH = "crop_disease_model.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static final String DATABASE_URL = "https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private ActivityCropQualityBinding binding;
    private Interpreter tflite;
    private List<String> labels;
    private Bitmap selectedBitmap;
    private String currentPhotoPath;
    private Uri currentPhotoUri;
    private String currentDisease = "Healthy";
    private int inputHeight, inputWidth;
    private TextToSpeech tts;

    // Activity Result Launchers
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) loadBitmap(uri); });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && currentPhotoUri != null) {
                    loadBitmap(currentPhotoUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityCropQualityBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            binding.toolbar.setNavigationOnClickListener(v -> finish());
            
            initTTS();
            initTfLite();
            setupListeners();

            if (savedInstanceState != null) {
                currentPhotoPath = savedInstanceState.getString("photo_path");
                if (currentPhotoPath != null) currentPhotoUri = Uri.fromFile(new File(currentPhotoPath));
            }
        } catch (Exception e) {
            Log.e(TAG, "Initialization failed", e);
            finish();
        }
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.US);
        });
    }

    private void initTfLite() {
        try {
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(this, MODEL_PATH);
            tflite = new Interpreter(modelBuffer, new Interpreter.Options());
            labels = FileUtil.loadLabels(this, LABEL_PATH);

            if (tflite != null) {
                int[] inputShape = tflite.getInputTensor(0).shape();
                inputHeight = inputShape[1];
                inputWidth = inputShape[2];
            }
        } catch (Exception e) {
            Log.e(TAG, "TFLite init failed", e);
            Toast.makeText(this, "Model loading failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        binding.ivCropPreview.setOnClickListener(v -> checkPermissionsAndLaunch(false));
        binding.btnGallery.setOnClickListener(v -> checkPermissionsAndLaunch(false));
        binding.btnCamera.setOnClickListener(v -> checkPermissionsAndLaunch(true));
        binding.btnAnalyze.setOnClickListener(v -> {
            if (selectedBitmap != null) runAnalysis();
            else Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkPermissionsAndLaunch(boolean isCamera) {
        String[] perms;
        if (isCamera) {
            perms = new String[]{Manifest.permission.CAMERA};
        } else {
            perms = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES} : 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        boolean allGranted = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false; break;
            }
        }

        if (allGranted) {
            if (isCamera) launchCamera(); else galleryLauncher.launch("image/*");
        } else {
            ActivityCompat.requestPermissions(this, perms, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.CAMERA)) launchCamera();
                else galleryLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchCamera() {
        try {
            File file = createImageFile();
            if (file != null) {
                currentPhotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                cameraLauncher.launch(currentPhotoUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Camera launch failed", e);
        }
    }

    private File createImageFile() throws IOException {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("QUAL_" + ts, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadBitmap(Uri uri) {
        binding.progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // Resize to avoid OOM
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                if (is != null) is.close();

                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (bitmap != null) {
                        selectedBitmap = bitmap;
                        binding.ivCropPreview.setImageBitmap(selectedBitmap);
                        binding.ivCropPreview.setAlpha(1.0f);
                        binding.layoutUploadHint.setVisibility(View.GONE);
                        binding.layoutResult.setVisibility(View.GONE);
                        binding.cardInvalid.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Bitmap load failed", e);
                runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
            }
        }).start();
    }

    private void runAnalysis() {
        if (tflite == null || selectedBitmap == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutResult.setVisibility(View.GONE);
        binding.btnAnalyze.setEnabled(false);

        new Thread(() -> {
            try {
                Bitmap scaled = Bitmap.createScaledBitmap(selectedBitmap, inputWidth, inputHeight, true);
                TensorImage ti = new TensorImage(DataType.FLOAT32);
                ti.load(scaled);
                ti = new ImageProcessor.Builder().add(new NormalizeOp(0.0f, 255.0f)).build().process(ti);

                TensorBuffer ob = TensorBuffer.createFixedSize(tflite.getOutputTensor(0).shape(), DataType.FLOAT32);
                tflite.run(ti.getBuffer(), ob.getBuffer());

                float[] probs = ob.getFloatArray();
                int maxIdx = -1; float maxProb = -1f;
                for (int i = 0; i < probs.length; i++) {
                    if (probs[i] > maxProb) { maxProb = probs[i]; maxIdx = i; }
                }

                final int fIdx = maxIdx; final float fConf = maxProb;

                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAnalyze.setEnabled(true);
                    if (fIdx != -1 && fConf >= CONFIDENCE_THRESHOLD) {
                        showDetailedResult(labels.get(fIdx), fConf);
                    } else {
                        binding.layoutResult.setVisibility(View.VISIBLE);
                        binding.layoutDetails.setVisibility(View.GONE);
                        binding.cardInvalid.setVisibility(View.VISIBLE);
                        binding.tvDetectedCrop.setText("Unknown");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Analysis error", e);
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAnalyze.setEnabled(true);
                    Toast.makeText(this, "Analysis failed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showDetailedResult(String label, float conf) {
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.layoutDetails.setVisibility(View.VISIBLE);
        binding.cardInvalid.setVisibility(View.GONE);
        binding.layoutResult.setAlpha(0f);
        binding.layoutResult.animate().alpha(1f).setDuration(500).start();

        String crop = label.split("___")[0].replace("_", " ");
        binding.tvDetectedCrop.setText("Detected: " + crop);
        
        int score = (int)(conf * 100);
        binding.qualityProgress.setProgress(score);
        binding.tvQualityPercentage.setText(score + "%");

        String grade = score > 80 ? "A Grade" : (score > 50 ? "B Grade" : "C Grade");
        binding.tvGrade.setText(grade);
        binding.tvFreshness.setText(score > 70 ? "High" : "Fair");

        currentDisease = label.contains("___") ? label.split("___")[1].replace("_", " ") : "Healthy";
        binding.cardDiseaseRecommendation.setVisibility(View.VISIBLE);
        binding.tvDiseaseStatus.setText("Status: " + currentDisease);
        
        String recommendation = currentDisease.toLowerCase().contains("healthy") ? 
                "Your crop is healthy. Maintain regular irrigation and organic composting." : 
                "Action Needed: Detected " + currentDisease + ". Apply recommended fungicides and isolate infected plants.";
        binding.tvFertilizerRecommendation.setText(recommendation);

        String priceText = "₹" + (score * 2) + " / kg";
        binding.tvMarketPrice.setText(priceText);
        
        String insight = "Market demand for " + crop + " is stable. Your " + grade + " quality can fetch premium prices.";
        binding.tvSuggestions.setText(insight);
        
        speakResult(crop, grade, priceText);
        saveToFirebase(crop, grade, priceText, score + "%", recommendation);
    }

    private void saveToFirebase(String c, String g, String p, String per, String rec) {
        try {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u == null) return;

            DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_URL).getReference("CropQuality");
            String id = ref.push().getKey();
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            
            CropQuality data = new CropQuality(id, c, g, p, per, currentDisease, rec, u.getEmail(), u.getUid(), ts);

            if (id != null) ref.child(id).setValue(data.toMap());
        } catch (Exception e) { 
            Log.e(TAG, "Firebase save error", e); 
        }
    }

    private void speakResult(String c, String g, String p) {
        if (tts != null) tts.speak("Crop " + c + " is " + g + " with estimated price " + p, TextToSpeech.QUEUE_FLUSH, null, "v");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", currentPhotoPath);
    }

    @Override
    protected void onDestroy() {
        if (tflite != null) tflite.close();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
