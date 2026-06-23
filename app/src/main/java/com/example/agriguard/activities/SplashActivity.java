package com.example.agriguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agriguard.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LinearLayout logoContainer = findViewById(R.id.logo_container);
        MaterialButton btnGetStarted = findViewById(R.id.btn_get_started);

        AlphaAnimation fade = new AlphaAnimation(0.0f, 1.0f);
        fade.setDuration(1500);
        logoContainer.startAnimation(fade);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Auto login
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                finish();
            }, 2000);
        } else {
            btnGetStarted.setOnClickListener(v -> {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            });
        }
    }
}
