package com.example.agrinova;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PredictionViewModel extends AndroidViewModel {
    private FirebaseManager firebaseManager;
    private PricePredictor pricePredictor;

    private MutableLiveData<List<PredictionModel>> predictions = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public PredictionViewModel(@NonNull Application application) {
        super(application);
        firebaseManager = new FirebaseManager();
        pricePredictor = new PricePredictor(application);
        loadPredictions();
    }

    public LiveData<List<PredictionModel>> getPredictions() { return predictions; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void predictAndSave(String state, String district, String market, String commodity, 
                               String variety, int year, int month, int day) {
        if (!pricePredictor.isInitialized()) {
            error.setValue("Model is still loading, please wait...");
            return;
        }
        isLoading.setValue(true);
        new Thread(() -> {
            try {
                float predictedPrice = pricePredictor.predict(state, district, market, commodity, variety, year, month, day);
                
                String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day);
                PredictionModel model = new PredictionModel(
                        null, state, district, market, commodity, variety, predictedPrice, date, System.currentTimeMillis()
                );

                firebaseManager.savePrediction(model, new FirebaseManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onFailure(String err) {
                        isLoading.postValue(false);
                        error.postValue(err);
                    }
                });
            } catch (Exception e) {
                isLoading.postValue(false);
                error.postValue("Prediction failed: " + e.getMessage());
            }
        }).start();
    }

    private void loadPredictions() {
        firebaseManager.observePredictions(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PredictionModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PredictionModel pm = ds.getValue(PredictionModel.class);
                    if (pm != null) list.add(pm);
                }
                Collections.reverse(list); // Newest first
                predictions.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                error.setValue(e.getMessage());
            }
        });
    }

    public PricePredictor getPricePredictor() {
        return pricePredictor;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (pricePredictor != null) {
            pricePredictor.close();
        }
    }
}
