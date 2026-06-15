package com.example.agrinova;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<PrefsManager.HistoryItem> historyList;
    private final OnHistoryActionListener listener;

    public interface OnHistoryActionListener {
        void onDelete(int position);
        void onShare(PrefsManager.HistoryItem item);
    }

    public HistoryAdapter(List<PrefsManager.HistoryItem> historyList, OnHistoryActionListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrefsManager.HistoryItem item = historyList.get(position);
        holder.tvDisease.setText(item.diseaseName);
        holder.tvConfidence.setText(String.format(Locale.getDefault(), "Confidence: %s", item.confidence));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(item.timestamp)));

        if (item.imageUri != null) {
            try {
                // Using Glide for better image loading from Uri strings
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(Uri.parse(item.imageUri))
                        .placeholder(R.drawable.ic_logo)
                        .into(holder.ivImage);
            } catch (Exception ignored) {}
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(position);
        });

        holder.btnShare.setOnClickListener(v -> {
            if (listener != null) listener.onShare(item);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivImage;
        TextView tvDisease, tvConfidence, tvDate;
        android.view.View btnDelete, btnShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_history_image);
            tvDisease = itemView.findViewById(R.id.tv_history_disease);
            tvConfidence = itemView.findViewById(R.id.tv_history_conf);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_history);
            btnShare = itemView.findViewById(R.id.btn_share_history);
        }
    }
}
