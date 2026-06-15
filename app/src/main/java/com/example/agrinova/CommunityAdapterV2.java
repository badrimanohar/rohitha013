package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agrinova.databinding.ItemCommunityCardV2Binding;
import java.util.List;

public class CommunityAdapterV2 extends RecyclerView.Adapter<CommunityAdapterV2.ViewHolder> {

    private List<Community> communities;
    private OnCommunityClickListener listener;

    public interface OnCommunityClickListener {
        void onJoinClick(Community community);
    }

    public CommunityAdapterV2(List<Community> communities, OnCommunityClickListener listener) {
        this.communities = communities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommunityCardV2Binding binding = ItemCommunityCardV2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Community community = communities.get(position);
        holder.binding.tvName.setText(community.getName());
        holder.binding.tvMembers.setText(community.getMemberCount() + " Members");

        if (community.getImageUrl() != null && !community.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(community.getImageUrl()).placeholder(R.drawable.ic_logo).into(holder.binding.ivCrop);
        } else {
            holder.binding.ivCrop.setImageResource(community.getIconResId() != 0 ? community.getIconResId() : R.drawable.ic_logo);
        }

        holder.binding.btnJoin.setOnClickListener(v -> listener.onJoinClick(community));
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCommunityCardV2Binding binding;
        public ViewHolder(ItemCommunityCardV2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
