package com.example.agriguard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agriguard.R;
import com.example.agriguard.models.Feature;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {

    private List<Feature> featureList;

    public FeatureAdapter(List<Feature> featureList) {
        this.featureList = featureList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feature feature = featureList.get(position);
        holder.tvTitle.setText(feature.getTitle());
        holder.tvDesc.setText(feature.getDescription());
        holder.ivIcon.setImageResource(feature.getIconRes());
        holder.btnAction.setText(feature.getButtonText());

        View.OnClickListener clickListener = v -> {
            String title = feature.getTitle();
            android.content.Context context = v.getContext();
            
            if (title.equals(context.getString(R.string.disease_detection_title))) {
                if (context instanceof com.example.agriguard.activities.DashboardActivity) {
                    ((com.example.agriguard.activities.DashboardActivity) context).navigateToFragment(R.id.nav_detect);
                } else {
                    context.startActivity(new android.content.Intent(context, com.example.agriguard.activities.DiseaseDetectionActivity.class));
                }
            } else if (title.equals("Crop Community")) {
                if (context instanceof com.example.agriguard.activities.DashboardActivity) {
                    ((com.example.agriguard.activities.DashboardActivity) context).navigateToFragment(R.id.nav_community);
                }
            } else if (title.equals("Crop Prices")) {
                if (context instanceof com.example.agriguard.activities.DashboardActivity) {
                    ((com.example.agriguard.activities.DashboardActivity) context).navigateToFragment(R.id.nav_prices);
                }
            } else if (title.equals("Crop History")) {
                if (context instanceof com.example.agriguard.activities.DashboardActivity) {
                    ((com.example.agriguard.activities.DashboardActivity) context).showHistory();
                }
            }
        };

        holder.btnAction.setOnClickListener(clickListener);
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return featureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDesc;
        MaterialButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_feature_icon);
            tvTitle = itemView.findViewById(R.id.tv_feature_title);
            tvDesc = itemView.findViewById(R.id.tv_feature_desc);
            btnAction = itemView.findViewById(R.id.btn_feature_action);
        }
    }
}