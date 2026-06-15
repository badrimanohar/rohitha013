package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agrinova.databinding.ItemCommunityListBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {

    private List<Community> communities;
    private OnCommunityClickListener listener;

    public interface OnCommunityClickListener {
        void onJoinClick(Community community);
    }

    public CommunityAdapter(List<Community> communities, OnCommunityClickListener listener) {
        this.communities = communities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommunityListBinding binding = ItemCommunityListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Community community = communities.get(position);
        holder.binding.tvCommunityName.setText(community.getName());
        
        if (community.isJoined()) {
            holder.binding.tvLastMessage.setText(community.getLastMessage() != null ? community.getLastMessage() : "No messages yet");
            holder.binding.tvLastMessage.setVisibility(View.VISIBLE);
            
            if (community.getLastMessageTime() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                holder.binding.tvTime.setText(sdf.format(new Date(community.getLastMessageTime())));
                holder.binding.tvTime.setVisibility(View.VISIBLE);
            } else {
                holder.binding.tvTime.setVisibility(View.GONE);
            }

            if (community.getUnreadCount() > 0) {
                holder.binding.tvUnreadCount.setText(String.valueOf(community.getUnreadCount()));
                holder.binding.tvUnreadCount.setVisibility(View.VISIBLE);
            } else {
                holder.binding.tvUnreadCount.setVisibility(View.GONE);
            }
        } else {
            holder.binding.tvLastMessage.setText(community.getMemberCount() + "+ Farmers");
            holder.binding.tvTime.setVisibility(View.GONE);
            holder.binding.tvUnreadCount.setVisibility(View.GONE);
        }

        if (community.getIconResId() != 0) {
            holder.binding.ivCommunityIcon.setImageResource(community.getIconResId());
        } else {
            holder.binding.ivCommunityIcon.setImageResource(R.drawable.ic_logo);
        }

        holder.itemView.setOnClickListener(v -> listener.onJoinClick(community));
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    public void updateList(List<Community> newList) {
        this.communities = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCommunityListBinding binding;
        public ViewHolder(ItemCommunityListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
