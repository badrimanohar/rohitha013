package com.example.agrinova;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agrinova.databinding.ItemLanguageBinding;
import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    private List<Language> languages;
    private int selectedPosition = -1;

    public LanguageAdapter(List<Language> languages) {
        this.languages = languages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLanguageBinding binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (languages == null || position >= languages.size()) return;
        
        try {
            Language language = languages.get(position);
            if (language == null) return;
            
            holder.binding.tvLangName.setText(language.getName());

            if (selectedPosition == position) {
                holder.binding.cardLanguage.setStrokeWidth(6);
                holder.binding.cardLanguage.setStrokeColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_yellow));
                holder.binding.cardLanguage.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_green));
                holder.binding.tvLangName.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                holder.binding.tvLangName.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20);
            } else {
                holder.binding.cardLanguage.setStrokeWidth(0);
                holder.binding.cardLanguage.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                holder.binding.tvLangName.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_green));
                holder.binding.tvLangName.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
            }

            holder.itemView.setOnClickListener(v -> {
                try {
                    int previousSelected = selectedPosition;
                    selectedPosition = holder.getBindingAdapterPosition();
                    if (selectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelected);
                        notifyItemChanged(selectedPosition);
                        
                        // Interactive scale animation
                        v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).withEndAction(() -> {
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        }).start();
                    }
                } catch (Exception e) {
                    android.util.Log.e("AgriNova", "Click interaction failed", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AgriNova", "Binding view holder failed", e);
        }
    }

    @Override
    public int getItemCount() {
        return languages != null ? languages.size() : 0;
    }

    public String getSelectedLanguage() {
        if (languages != null && selectedPosition != -1 && selectedPosition < languages.size()) {
            Language lang = languages.get(selectedPosition);
            return lang != null ? lang.getName() : null;
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemLanguageBinding binding;

        public ViewHolder(ItemLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
