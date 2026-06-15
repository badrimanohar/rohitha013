package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agrinova.databinding.ItemChatMessageReceivedBinding;
import com.example.agrinova.databinding.ItemChatMessageSentBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapterV2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messages;
    private final String currentUserId;
    private final OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageLongClick(ChatMessage message);
        void onImageClick(String imageUrl);
    }

    public ChatAdapterV2(List<ChatMessage> messages, OnMessageClickListener listener) {
        this.messages = messages;
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            ItemChatMessageSentBinding binding = ItemChatMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemChatMessageReceivedBinding binding = ItemChatMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = timeFmt.format(new Date(msg.getTimestamp()));

        if (holder instanceof SentViewHolder) {
            ItemChatMessageSentBinding binding = ((SentViewHolder) holder).binding;
            binding.tvSenderName.setText("Me");
            binding.tvMessage.setText(msg.getMessage());
            binding.tvTime.setText(time);
            
            if (msg.getMediaUrl() != null && "image".equals(msg.getMediaType())) {
                binding.ivImage.setVisibility(View.VISIBLE);
                Glide.with(binding.ivImage.getContext()).load(msg.getMediaUrl()).into(binding.ivImage);
                binding.ivImage.setOnClickListener(v -> listener.onImageClick(msg.getMediaUrl()));
            } else {
                binding.ivImage.setVisibility(View.GONE);
            }

            if (msg.getReplyId() != null) {
                binding.layoutReply.setVisibility(View.VISIBLE);
                binding.tvReplyName.setText(msg.getReplySender());
                binding.tvReplyText.setText(msg.getReplyMessage());
            } else {
                binding.layoutReply.setVisibility(View.GONE);
            }

            // Message Status
            ImageView ivStatus = holder.itemView.findViewById(R.id.iv_status);
            if (ivStatus != null) {
                if (msg.isSeen()) {
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    ivStatus.setColorFilter(android.graphics.Color.parseColor("#4FC3F7")); // Blue
                } else {
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    ivStatus.setColorFilter(android.graphics.Color.parseColor("#888888")); // Grey
                }
            }

            holder.itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClick(msg);
                return true;
            });

        } else if (holder instanceof ReceivedViewHolder) {
            ItemChatMessageReceivedBinding binding = ((ReceivedViewHolder) holder).binding;
            binding.tvSenderName.setText(msg.getSenderName());
            binding.tvMessage.setText(msg.getMessage());
            binding.tvTime.setText(time);

            if (msg.getProfileImage() != null && !msg.getProfileImage().isEmpty()) {
                Glide.with(binding.ivProfile.getContext()).load(msg.getProfileImage()).placeholder(R.drawable.ic_logo).into(binding.ivProfile);
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_logo);
            }

            if (msg.getMediaUrl() != null && "image".equals(msg.getMediaType())) {
                binding.ivImage.setVisibility(View.VISIBLE);
                Glide.with(binding.ivImage.getContext()).load(msg.getMediaUrl()).into(binding.ivImage);
                binding.ivImage.setOnClickListener(v -> listener.onImageClick(msg.getMediaUrl()));
            } else {
                binding.ivImage.setVisibility(View.GONE);
            }

            if (msg.getReplyId() != null) {
                binding.layoutReply.setVisibility(View.VISIBLE);
                binding.tvReplyName.setText(msg.getReplySender());
                binding.tvReplyText.setText(msg.getReplyMessage());
            } else {
                binding.layoutReply.setVisibility(View.GONE);
            }

            // Message Status
            ImageView ivStatus = holder.itemView.findViewById(R.id.iv_status);
            if (ivStatus != null) {
                if (msg.isSeen()) {
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    ivStatus.setColorFilter(android.graphics.Color.parseColor("#4FC3F7")); // Blue
                } else {
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    ivStatus.setColorFilter(android.graphics.Color.parseColor("#888888")); // Grey
                }
            }

            holder.itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClick(msg);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        ItemChatMessageSentBinding binding;
        SentViewHolder(ItemChatMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ItemChatMessageReceivedBinding binding;
        ReceivedViewHolder(ItemChatMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
