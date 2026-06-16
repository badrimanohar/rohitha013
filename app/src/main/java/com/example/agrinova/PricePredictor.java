package com.example.agrinova;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PricePredictor {
    private static final String TAG = "PricePredictor";
    private static final String MODEL_FILE = "crop_price_model.tflite";
    private static final String MAPPING_FILE = "price_mappings.json";

    private Interpreter tflite;
    private Map<String, Map<String, Integer>> mappings = new HashMap<>();
    private boolean isInitialized = false;

    public PricePredictor(Context context) {
        try {
            Interpreter.Options options = new Interpreter.Options();
            tflite = new Interpreter(ModelLoader.loadModelFile(context, MODEL_FILE), options);
            isInitialized = true;
            Log.d(TAG, "TFLite Model loaded successfully");
            loadMappings(context);
            Log.d(TAG, "Mappings loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing PricePredictor", e);
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private void loadMappings(Context context) throws Exception {
        String json;
        try (java.io.InputStream is = context.getAssets().open(MAPPING_FILE)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int read = is.read(buffer);
            if (read > 0) {
                json = new String(buffer, 0, read, StandardCharsets.UTF_8);
            } else {
                json = "{}";
            }
        }

        JSONObject obj = new JSONObject(json);
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject innerObj = obj.getJSONObject(key);
            Map<String, Integer> innerMap = new HashMap<>();
            Iterator<String> innerKeys = innerObj.keys();
            while (innerKeys.hasNext()) {
                String innerKey = innerKeys.next();
                innerMap.put(innerKey, innerObj.getInt(innerKey));
            }
            mappings.put(key, innerMap);
        }
    }

    public float predict(String state, String district, String market, String commodity, String variety, 
                         int year, int month, int day) {
        if (tflite == null) return -1;

        float[][] input = new float[1][8];
        input[0][0] = getMappingValue("states", state);
        input[0][1] = getMappingValue("districts", district);
        input[0][2] = getMappingValue("markets", market);
        input[0][3] = getMappingValue("commodities", commodity);
        input[0][4] = getMappingValue("varieties", variety);
        input[0][5] = (float) year;
        input[0][6] = (float) month;
        input[0][7] = (float) day;

        float[][] output = new float[1][1];
        tflite.run(input, output);

        return output[0][0];
    }

    private float getMappingValue(String category, String value) {
        Map<String, Integer> categoryMap = mappings.get(category);
        if (categoryMap != null) {
            Integer val = categoryMap.get(value);
            if (val != null) {
                return (float) val;
            }
        }
        return 0.0f; // Default if not found
    }

    public Map<String, Map<String, Integer>> getMappings() {
        return mappings;
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }
}
