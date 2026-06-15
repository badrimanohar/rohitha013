package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DetectionHistoryAdapter extends RecyclerView.Adapter<DetectionHistoryAdapter.ViewHolder> {

    private List<CropDetection> detections;

    public DetectionHistoryAdapter(List<CropDetection> detections) {
        this.detections = detections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detection_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CropDetection detection = detections.get(position);
        holder.tvCropName.setText(detection.getCropName());
        holder.tvDiseaseName.setText("Disease: " + detection.getDiseaseName());
        holder.tvTreatment.setText(detection.getTreatment());
        
        String ts = detection.getTimestamp();
        try {
            long millis = Long.parseLong(ts);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(new java.util.Date(millis)));
        } catch (Exception e) {
            holder.tvTimestamp.setText(ts);
        }
    }

    @Override
    public int getItemCount() {
        return detections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName, tvDiseaseName, tvTreatment, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tv_crop_name);
            tvDiseaseName = itemView.findViewById(R.id.tv_disease_name);
            tvTreatment = itemView.findViewById(R.id.tv_treatment);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
