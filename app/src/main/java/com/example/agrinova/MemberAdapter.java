package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agrinova.databinding.ItemMemberBinding;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<User> members;

    public MemberAdapter(List<User> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberBinding binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = members.get(position);
        holder.binding.tvMemberName.setText(user.getName());
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(holder.binding.ivMemberProfile.getContext()).load(user.getProfileImage()).placeholder(R.drawable.ic_logo).into(holder.binding.ivMemberProfile);
        } else {
            holder.binding.ivMemberProfile.setImageResource(R.drawable.ic_logo);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemMemberBinding binding;
        public ViewHolder(ItemMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
