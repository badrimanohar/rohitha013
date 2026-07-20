package com.example.agriguard.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agriguard.R;
import com.example.agriguard.models.PredictionResult;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<PredictionResult> historyList;
    private OnHistoryDeleteListener deleteListener;

    public interface OnHistoryDeleteListener {
        void onDeleteClick(PredictionResult result);
    }

    public HistoryAdapter(List<PredictionResult> historyList, OnHistoryDeleteListener deleteListener) {
        this.historyList = historyList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredictionResult result = historyList.get(position);
        holder.tvCropName.setText(result.getCropName());
        holder.tvDiseaseStatus.setText(result.getDiseaseName());
        holder.tvDate.setText(DateFormat.format("dd MMM yyyy, hh:mm a", result.getTimestamp()));
        
        if (result.getStatus().equalsIgnoreCase("Healthy")) {
            holder.tvDiseaseStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_green));
        } else {
            holder.tvDiseaseStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
        }

        if (result.getImageUrl() != null && !result.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(result.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.ivHistory);
        } else {
            holder.ivHistory.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateData(List<PredictionResult> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName, tvDate, tvDiseaseStatus;
        ImageView ivHistory;
        View btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tv_crop_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDiseaseStatus = itemView.findViewById(R.id.tv_disease_status);
            ivHistory = itemView.findViewById(R.id.iv_history);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
