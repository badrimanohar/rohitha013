package com.example.agriguard.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class PriceViewModel extends AndroidViewModel {

    private final DatabaseReference mDatabase;
    private final String userId;

    private final MutableLiveData<PredictionResult> _predictionResult = new MutableLiveData<>();
    public final LiveData<PredictionResult> predictionResult = _predictionResult;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public PriceViewModel(@NonNull Application application) {
        super(application);
        mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        userId = FirebaseAuth.getInstance().getUid();
    }

    public void predictPrice(String state, String district, String market, String crop) {
        // In a real TFLite implementation, we would encode these strings into numeric values
        // using a LabelEncoder or Dictionary mapping, then run through the .tflite model.
        
        // Mocking the TFLite prediction for this logic flow
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate processing
                
                // Simulated AI Result
                double predictedPrice = 2000 + (Math.random() * 5000);
                float confidence = 0.85f + (float)(Math.random() * 0.10f);
                String trend = Math.random() > 0.5 ? "High" : "Medium";
                String recommendation = trend.equals("High") ? "Sell Now" : "Wait for Better Price";

                PredictionResult result = new PredictionResult(
                    state, district, market, crop, predictedPrice, confidence, trend, recommendation
                );

                _predictionResult.postValue(result);
                savePredictionToFirebase(result);
            } catch (InterruptedException e) {
                _error.postValue("Prediction interrupted");
            }
        }).start();
    }

    private void savePredictionToFirebase(PredictionResult result) {
        if (userId == null) return;

        String id = mDatabase.child("price_predictions").push().getKey();
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("state", result.state);
            data.put("district", result.district);
            data.put("market", result.market);
            data.put("crop", result.crop);
            data.put("predictedPrice", result.price);
            data.put("confidence", result.confidence);
            data.put("timestamp", System.currentTimeMillis());

            mDatabase.child("price_predictions").child(id).setValue(data);
        }
    }

    public static class PredictionResult {
        public String state, district, market, crop, trend, recommendation;
        public double price;
        public float confidence;

        public PredictionResult(String state, String district, String market, String crop, 
                                double price, float confidence, String trend, String recommendation) {
            this.state = state;
            this.district = district;
            this.market = market;
            this.crop = crop;
            this.price = price;
            this.confidence = confidence;
            this.trend = trend;
            this.recommendation = recommendation;
        }
    }
}
