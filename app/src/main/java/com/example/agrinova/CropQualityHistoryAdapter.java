package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CropQualityHistoryAdapter extends RecyclerView.Adapter<CropQualityHistoryAdapter.ViewHolder> {

    private List<CropQuality> records;

    public CropQualityHistoryAdapter(List<CropQuality> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_quality_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CropQuality q = records.get(position);
        holder.tvCropName.setText(q.getCropName());
        holder.tvGrade.setText(q.getQualityGrade());
        holder.tvPrice.setText(q.getMarketPrice());
        holder.tvPercentage.setText(q.getQualityPercentage());
        holder.tvTimestamp.setText(q.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName, tvGrade, tvPrice, tvPercentage, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tv_crop_name);
            tvGrade = itemView.findViewById(R.id.tv_quality_grade);
            tvPrice = itemView.findViewById(R.id.tv_market_price);
            tvPercentage = itemView.findViewById(R.id.tv_quality_percentage);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
