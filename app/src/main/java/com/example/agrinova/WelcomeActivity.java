package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agrinova.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            binding.btnWelcomeLogin.setOnClickListener(v -> {
                AnimUtils.pressAnimation(v);
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            });

            binding.btnWelcomeSignup.setOnClickListener(v -> {
                AnimUtils.pressAnimation(v);
                Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            android.util.Log.e("AgriNova", "WelcomeActivity initialization failed", e);
            finish();
        }
    }
}
