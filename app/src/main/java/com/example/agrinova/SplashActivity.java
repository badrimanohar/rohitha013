package com.example.agrinova;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agrinova.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            // View Binding
            binding = ActivitySplashBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Fade Animation for Logo
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

            if (fadeIn != null) {
                fadeIn.setDuration(1500);

                if (binding.logo != null) {
                    binding.logo.startAnimation(fadeIn);
                }
            }

            // Slide Animation
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

            if (slideUp != null) {

                if (binding.appName != null) {
                    binding.appName.startAnimation(slideUp);
                }

                if (binding.tagline != null) {
                    binding.tagline.startAnimation(slideUp);
                }
            }

            // Splash Delay
            new Handler().postDelayed(() -> {

                try {
                    PrefsManager prefs = new PrefsManager(this);

                    // Check Persistent Login Session
                    if (prefs.isLoggedIn() || mAuth.getCurrentUser() != null) {

                        // User Already Logged In
                        Intent dashboardIntent =
                                new Intent(SplashActivity.this,
                                        DashboardActivity.class);

                        startActivity(dashboardIntent);

                    } else {

                        // New User or Logged Out -> Welcome Landing
                        Intent welcomeIntent =
                                new Intent(SplashActivity.this,
                                        WelcomeActivity.class);

                        startActivity(welcomeIntent);
                    }

                    finish();

                } catch (Exception e) {

                    e.printStackTrace();

                    // Fallback to Welcome Page
                    Intent welcomeIntent =
                            new Intent(SplashActivity.this,
                                    WelcomeActivity.class);

                    startActivity(welcomeIntent);

                    finish();
                }

            }, 2500);

        } catch (Exception e) {

            e.printStackTrace();

            // Fallback to Login Page
            Intent loginIntent =
                    new Intent(this,
                            LoginActivity.class);

            startActivity(loginIntent);

            finish();
        }
    }

}
