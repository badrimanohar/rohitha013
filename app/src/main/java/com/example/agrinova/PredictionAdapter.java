package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agrinova.databinding.ItemPredictionBinding;
import java.util.List;
import java.util.Locale;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {

    private List<PredictionModel> predictions;

    public PredictionAdapter(List<PredictionModel> predictions) {
        this.predictions = predictions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPredictionBinding binding = ItemPredictionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredictionModel pm = predictions.get(position);
        holder.binding.tvCommodity.setText(pm.getCommodity());
        holder.binding.tvPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", pm.getPredictedPrice()));
        holder.binding.tvDate.setText(pm.getPredictionDate());
        holder.binding.tvLocation.setText(String.format("%s, %s", pm.getDistrict(), pm.getState()));
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public void setPredictions(List<PredictionModel> predictions) {
        this.predictions = predictions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPredictionBinding binding;
        ViewHolder(ItemPredictionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
