package com.example.agrinova;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Helper class for TensorFlow Lite operations for Crop Disease Detection.
 */
public class TFLiteHelper {

    private static final String TAG = "AgriNova_TFLite";
    private static final float CONFIDENCE_THRESHOLD = 0.40f;

    private final Context context;
    private Interpreter interpreter;
    private List<String> labels = new ArrayList<>();
    private int inputHeight = 0;
    private int inputWidth = 0;
    private int[] outputShape;

    public static class Recognition {
        private final String title;
        private final float confidence;
        private final String cropName;
        private final String diseaseName;

        public Recognition(String title, float confidence) {
            this.title = title;
            this.confidence = confidence;
            
            // Assuming label format: "Crop___Disease"
            if (title.contains("___")) {
                String[] parts = title.split("___");
                this.cropName = parts[0].replace("_", " ");
                this.diseaseName = parts[1].replace("_", " ");
            } else {
                this.cropName = title;
                this.diseaseName = "Healthy / Unknown";
            }
        }

        public String getTitle() { return title; }
        public float getConfidence() { return confidence; }
        public String getCropName() { return cropName; }
        public String getDiseaseName() { return diseaseName; }
    }

    public TFLiteHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean init(String modelPath, String labelPath) {
        try {
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, modelPath);
            Interpreter.Options options = new Interpreter.Options();
            interpreter = new Interpreter(modelBuffer, options);
            labels = FileUtil.loadLabels(context, labelPath);

            if (interpreter != null) {
                int[] inShape = interpreter.getInputTensor(0).shape();
                inputHeight = inShape[1];
                inputWidth = inShape[2];
                outputShape = interpreter.getOutputTensor(0).shape();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load model/labels: " + e.getMessage());
        }
        return false;
    }

    public List<Recognition> runInference(Bitmap bitmap) {
        if (interpreter == null || bitmap == null) return new ArrayList<>();

        try {
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(0.0f, 255.0f))
                    .build();

            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);

            TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);
            interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

            float[] probabilities = outputBuffer.getFloatArray();
            PriorityQueue<Recognition> pq = new PriorityQueue<>(3, (a, b) -> Float.compare(b.getConfidence(), a.getConfidence()));

            for (int i = 0; i < Math.min(probabilities.length, labels.size()); i++) {
                if (probabilities[i] >= CONFIDENCE_THRESHOLD) {
                    pq.add(new Recognition(labels.get(i), probabilities[i]));
                }
            }

            List<Recognition> results = new ArrayList<>();
            for (int i = 0; i < 3 && !pq.isEmpty(); i++) {
                results.add(pq.poll());
            }
            return results;
        } catch (Exception e) {
            Log.e(TAG, "Inference error", e);
            return new ArrayList<>();
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
