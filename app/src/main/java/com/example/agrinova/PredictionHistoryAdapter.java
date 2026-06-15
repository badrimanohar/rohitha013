package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agrinova.databinding.ItemHistoryBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PredictionHistoryAdapter extends RecyclerView.Adapter<PredictionHistoryAdapter.ViewHolder> {

    private List<PredictionRecord> records;

    public PredictionHistoryAdapter(List<PredictionRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredictionRecord record = records.get(position);
        
        holder.binding.tvHistoryDisease.setText(record.getCropName() + " (" + record.getDiseaseName() + ")");
        holder.binding.tvHistoryConf.setText("Confidence: " + record.getConfidence());
        
        String date = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date(record.getTimestamp()));
        holder.binding.tvHistoryDate.setText(date);

        Glide.with(holder.itemView.getContext())
                .load(record.getImageUrl())
                .placeholder(R.drawable.ic_logo)
                .into(holder.binding.ivHistoryImage);

        // Remove delete from Firebase history for now, or implement it
        holder.binding.btnDeleteHistory.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHistoryBinding binding;
        ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
