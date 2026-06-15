package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agrinova.databinding.ItemMyCommunityCardBinding;
import java.util.List;

public class MyCommunityAdapter extends RecyclerView.Adapter<MyCommunityAdapter.ViewHolder> {

    private List<Community> communities;
    private OnCommunityClickListener listener;

    public interface OnCommunityClickListener {
        void onOpenChatClick(Community community);
    }

    public MyCommunityAdapter(List<Community> communities, OnCommunityClickListener listener) {
        this.communities = communities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyCommunityCardBinding binding = ItemMyCommunityCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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

        holder.binding.btnOpenChat.setOnClickListener(v -> listener.onOpenChatClick(community));
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemMyCommunityCardBinding binding;
        public ViewHolder(ItemMyCommunityCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
