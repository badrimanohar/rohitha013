package com.example.agriguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agriguard.R;
import com.example.agriguard.activities.DashboardActivity;
import com.example.agriguard.adapters.FeatureAdapter;
import com.example.agriguard.adapters.HistoryAdapter;
import com.example.agriguard.models.Feature;
import com.example.agriguard.models.PredictionResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvRecentHistory;
    private HistoryAdapter historyAdapter;
    private DatabaseReference mDatabase;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Features Grid
        RecyclerView rvFeatures = view.findViewById(R.id.rv_features);
        rvFeatures.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<Feature> features = new ArrayList<>();
        features.add(new Feature(getString(R.string.disease_detection_title), getString(R.string.disease_detection_subtitle), android.R.drawable.ic_menu_camera, getString(R.string.scan_crop)));
        features.add(new Feature("Crop Community", "Farmers can share experiences, ask questions and discuss farming", android.R.drawable.ic_menu_share, getString(R.string.open_community)));
        features.add(new Feature("Crop Prices", "View current crop market prices and trends", android.R.drawable.ic_menu_sort_by_size, getString(R.string.view_prices)));
        features.add(new Feature("Crop History", "View previous scans and reports", android.R.drawable.ic_menu_recent_history, getString(R.string.view_reports)));

        FeatureAdapter adapter = new FeatureAdapter(features);
        rvFeatures.setAdapter(adapter);

        // Recent History
        rvRecentHistory = view.findViewById(R.id.rv_recent_history);
        rvRecentHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Disable delete in dashboard preview or handle it if needed
        historyAdapter = new HistoryAdapter(new ArrayList<>(), result -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).showHistory();
            }
        });
        rvRecentHistory.setAdapter(historyAdapter);

        TextView tvViewAll = view.findViewById(R.id.tv_view_all_history);
        tvViewAll.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).showHistory();
            }
        });

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://agriguard-29853-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

        loadUserName(view);
        loadRecentHistory();

        return view;
    }

    private void loadUserName(View view) {
        if (userId == null) return;
        
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        
        mDatabase.child("users").child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    tvGreeting.setText(getString(R.string.hello_farmer, name));
                } else {
                    tvGreeting.setText(getString(R.string.hello_farmer, "Farmer"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadRecentHistory() {
        if (userId == null) return;

        mDatabase.child("users").child(userId).child("history")
                .limitToLast(3)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PredictionResult> historyList = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        PredictionResult result = postSnapshot.getValue(PredictionResult.class);
                        if (result != null) {
                            historyList.add(result);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("HOME_HISTORY", "Parse error", e);
                    }
                }
                Collections.reverse(historyList);
                historyAdapter.updateData(historyList);
                
                // Show/hide history section based on data
                View llHistory = getView() != null ? getView().findViewById(R.id.ll_recent_history) : null;
                if (llHistory != null) {
                    llHistory.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silent fail for dashboard
            }
        });
    }
}
