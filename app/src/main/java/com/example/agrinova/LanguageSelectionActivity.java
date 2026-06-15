package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.agrinova.databinding.ActivityLanguageSelectionBinding;
import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionActivity extends AppCompatActivity {

    private ActivityLanguageSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityLanguageSelectionBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            List<Language> languages = new ArrayList<>();
            languages.add(new Language("English"));
            languages.add(new Language("Hindi"));
            languages.add(new Language("Telugu"));
            languages.add(new Language("Tamil"));
            languages.add(new Language("Kannada"));
            languages.add(new Language("Malayalam"));

            LanguageAdapter adapter = new LanguageAdapter(languages);
            binding.rvLanguages.setLayoutManager(new GridLayoutManager(this, 2));
            binding.rvLanguages.setAdapter(adapter);

            binding.btnContinue.setOnClickListener(v -> {
                String selected = adapter.getSelectedLanguage();
                if (selected != null) {
                    new PrefsManager(this).setLanguage(selected);
                    
                    if (getIntent().getBooleanExtra("fromSettings", false)) {
                        finish();
                    } else {
                        String target = getIntent().getStringExtra("target");
                        Intent intent;
                        if ("signup".equalsIgnoreCase(target)) {
                            intent = new Intent(LanguageSelectionActivity.this, SignupActivity.class);
                        } else {
                            intent = new Intent(LanguageSelectionActivity.this, LoginActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                } else {
                    android.widget.Toast.makeText(this, getString(R.string.select_language_toast), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AgriNova", "LanguageSelectionActivity initialization failed", e);
            finish();
        }
    }
}
