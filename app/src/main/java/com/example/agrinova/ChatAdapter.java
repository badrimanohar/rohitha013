package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.agrinova.databinding.ItemChatMessageReceivedBinding;
import com.example.agrinova.databinding.ItemChatMessageSentBinding;
import com.example.agrinova.databinding.ItemDateSeparatorBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_DATE = 3;

    private List<Object> items; // Can be ChatMessage or String (for date)
    private final String currentUserId;
    private final OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageLongClick(ChatMessage message);
        void onImageClick(String imageUrl);
        void onReplyClick(ChatMessage message);
    }

    public ChatAdapter(List<ChatMessage> messages, OnMessageClickListener listener) {
        this.currentUserId = FirebaseAuth.getInstance().getUid();
        this.listener = listener;
        setMessages(messages);
    }

    public void setMessages(List<ChatMessage> messages) {
        this.items = new ArrayList<>();
        if (messages == null || messages.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String lastDate = "";

        for (ChatMessage msg : messages) {
            String msgDate = sdf.format(new Date(msg.getTimestamp()));
            if (!msgDate.equals(lastDate)) {
                items.add(msgDate); // Add date separator
                lastDate = msgDate;
            }
            items.add(msg);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_DATE;
        } else {
            ChatMessage msg = (ChatMessage) item;
            return msg.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE) {
            ItemDateSeparatorBinding binding = ItemDateSeparatorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new DateViewHolder(binding);
        } else if (viewType == VIEW_TYPE_SENT) {
            ItemChatMessageSentBinding binding = ItemChatMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemChatMessageReceivedBinding binding = ItemChatMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).binding.tvDate.setText((String) item);
        } else if (holder instanceof SentViewHolder) {
            ChatMessage msg = (ChatMessage) item;
            ItemChatMessageSentBinding binding = ((SentViewHolder) holder).binding;
            
            binding.tvSenderName.setText("Me");
            binding.tvMessage.setText(msg.getMessage());
            binding.tvTime.setText(timeFmt.format(new Date(msg.getTimestamp())));
            
            // WhatsApp Ticks
            binding.ivStatus.setImageResource(R.drawable.ic_tick);
            binding.ivStatus.setColorFilter(msg.isSeen() ? 0xFF4FC3F7 : 0xFF888888);
            
            if (msg.getMediaUrl() != null && !msg.getMediaUrl().isEmpty()) {
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

            holder.itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClick(msg);
                return true;
            });

        } else if (holder instanceof ReceivedViewHolder) {
            ChatMessage msg = (ChatMessage) item;
            ItemChatMessageReceivedBinding binding = ((ReceivedViewHolder) holder).binding;

            binding.tvSenderName.setText(msg.getSenderName());
            binding.tvMessage.setText(msg.getMessage());
            binding.tvTime.setText(timeFmt.format(new Date(msg.getTimestamp())));

            if (msg.getProfileImage() != null && !msg.getProfileImage().isEmpty()) {
                Glide.with(binding.ivProfile.getContext()).load(msg.getProfileImage()).placeholder(R.drawable.ic_logo).into(binding.ivProfile);
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_logo);
            }

            if (msg.getMediaUrl() != null && !msg.getMediaUrl().isEmpty()) {
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

            holder.itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClick(msg);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void release() {
        // No specific release needed for this simplified adapter
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

    static class DateViewHolder extends RecyclerView.ViewHolder {
        ItemDateSeparatorBinding binding;
        DateViewHolder(ItemDateSeparatorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
