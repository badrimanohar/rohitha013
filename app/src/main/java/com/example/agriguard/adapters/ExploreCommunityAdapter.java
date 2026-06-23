package com.example.agriguard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agriguard.R;
import com.example.agriguard.models.Community;
import com.example.agriguard.utils.CropIconHelper;
import java.util.List;

public class ExploreCommunityAdapter extends RecyclerView.Adapter<ExploreCommunityAdapter.ViewHolder> {

    private List<Community> communities;
    private OnCommunityJoinListener listener;

    public interface OnCommunityJoinListener {
        void onJoinClick(Community community);
    }

    public ExploreCommunityAdapter(List<Community> communities, OnCommunityJoinListener listener) {
        this.communities = communities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_explore, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Community community = communities.get(position);
        holder.tvName.setText(community.getName());
        holder.tvMembers.setText(community.getMemberCount() + " Members");
        holder.tvDescription.setText(community.getDescription());
        holder.ivCommunity.setImageResource(CropIconHelper.getCropIcon(community.getName()));
        
        holder.btnJoin.setOnClickListener(v -> listener.onJoinClick(community));
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
        TextView tvName, tvMembers, tvDescription;
        ImageView ivCommunity;
        Button btnJoin, btnDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_community_name);
            tvMembers = itemView.findViewById(R.id.tv_members);
            tvDescription = itemView.findViewById(R.id.tv_description);
            ivCommunity = itemView.findViewById(R.id.iv_community);
            btnJoin = itemView.findViewById(R.id.btn_join);
            btnDetails = itemView.findViewById(R.id.btn_details);
        }
    }
}
