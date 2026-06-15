package com.example.agrinova;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class AnimUtils {
    public static void pressAnimation(View view) {
        if (view == null) return;
        try {
            ScaleAnimation scaleAnim = new ScaleAnimation(
                    1.0f, 0.95f, 
                    1.0f, 0.95f, 
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnim.setDuration(100);
            scaleAnim.setRepeatMode(Animation.REVERSE);
            scaleAnim.setRepeatCount(1);
            view.startAnimation(scaleAnim);
        } catch (Exception e) {
            android.util.Log.e("AgriNova", "Animation failed", e);
        }
    }

    public static void fadeIn(View view, int duration) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1f).setDuration(duration).setListener(null);
    }

    public static void slideUp(View view, int duration) {
        if (view == null) return;
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

    public static void shake(View view) {
        if (view == null) return;
        view.animate()
                .translationX(20f)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(-20f)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(20f)
                                .setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(0)
                                        .setDuration(50)
                                        .start())
                                .start())
                        .start())
                .start();
    }
}
