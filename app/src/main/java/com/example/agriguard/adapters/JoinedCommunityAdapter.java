package com.example.agriguard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agriguard.R;
import com.example.agriguard.models.Community;
import com.example.agriguard.utils.CropIconHelper;
import java.util.List;

public class JoinedCommunityAdapter extends RecyclerView.Adapter<JoinedCommunityAdapter.ViewHolder> {

    private List<Community> communities;
    private OnCommunityClickListener listener;

    public interface OnCommunityClickListener {
        void onCommunityClick(Community community);
    }

    public JoinedCommunityAdapter(List<Community> communities, OnCommunityClickListener listener) {
        this.communities = communities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_joined, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Community community = communities.get(position);
        holder.tvName.setText(community.getName());
        holder.tvMembers.setText(community.getMemberCount() + " Members");
        holder.tvLastMessage.setText(community.getLastMessage() != null ? community.getLastMessage() : "No messages yet");
        holder.ivCommunity.setImageResource(CropIconHelper.getCropIcon(community.getName()));
        
        holder.tvUnreadCount.setText(String.valueOf(community.getUnreadCount()));
        holder.tvUnreadCount.setVisibility(community.getUnreadCount() > 0 ? View.VISIBLE : View.GONE);
        
        holder.btnOpen.setOnClickListener(v -> listener.onCommunityClick(community));
        holder.itemView.setOnClickListener(v -> listener.onCommunityClick(community));
    }

    public void updateData(List<Community> newData) {
        this.communities = newData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvUnreadCount, tvMembers;
        ImageView ivCommunity;
        View btnOpen;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_community_name);
            tvMembers = itemView.findViewById(R.id.tv_members);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
            ivCommunity = itemView.findViewById(R.id.iv_community);
            btnOpen = itemView.findViewById(R.id.btn_open_chat);
        }
    }
}
