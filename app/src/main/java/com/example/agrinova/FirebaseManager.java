package com.example.agrinova;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {
    private static final String DATABASE_URL = "https://agrinova-9236c-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String NODE_PREDICTIONS = "MarketPricePredictions";

    private DatabaseReference mDatabase;

    public FirebaseManager() {
        mDatabase = FirebaseDatabase.getInstance(DATABASE_URL).getReference(NODE_PREDICTIONS);
    }

    public void savePrediction(PredictionModel prediction, OnCompleteListener listener) {
        String id = mDatabase.push().getKey();
        if (id != null) {
            prediction.setPredictionId(id);
            mDatabase.child(id).setValue(prediction)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        }
    }

    public void observePredictions(ValueEventListener listener) {
        mDatabase.orderByChild("timestamp").addValueEventListener(listener);
    }

    public Query getPredictionsQuery() {
        return mDatabase.orderByChild("timestamp");
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}
