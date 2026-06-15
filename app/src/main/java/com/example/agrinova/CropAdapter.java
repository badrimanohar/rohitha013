package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.ViewHolder> {

    private List<Crop> crops;
    private final OnCropClickListener listener;

    public interface OnCropClickListener {
        void onCropClick(Crop crop);
    }

    public CropAdapter(List<Crop> crops, OnCropClickListener listener) {
        this.crops = crops;
        this.listener = listener;
    }

    public void updateList(List<Crop> newList) {
        this.crops = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Crop crop = crops.get(position);
        holder.tvName.setText(crop.name);
        holder.tvCategory.setText(crop.category);
        holder.tvDesc.setText(crop.description);
        
        // Use a placeholder or a mapping logic for crop images
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(R.drawable.ic_logo) // Using logo as placeholder for all crops for now
                .into(holder.ivCrop);

        holder.itemView.setOnClickListener(v -> {
            AnimUtils.pressAnimation(v);
            listener.onCropClick(crop);
        });
    }

    @Override
    public int getItemCount() {
        return crops.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvDesc;
        android.widget.ImageView ivCrop;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_crop_name);
            tvCategory = itemView.findViewById(R.id.tv_crop_category);
            tvDesc = itemView.findViewById(R.id.tv_crop_desc);
            ivCrop = itemView.findViewById(R.id.iv_crop);
        }
    }
}
