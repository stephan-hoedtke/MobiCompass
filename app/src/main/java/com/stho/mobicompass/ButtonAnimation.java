package com.stho.mobicompass;

import android.animation.Animator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class ButtonAnimation {
    private final View view;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static ButtonAnimation build(View view) {
        return new ButtonAnimation(view);
    }

    void cleanup() {
        handler.removeCallbacksAndMessages(null);
    }

    private ButtonAnimation(View view) {
        this.view = view;
        hide();
    }

    void show() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::fadeIn, FADE_IN_DELAY);
    }

    void dismiss() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::fadeOut, FADE_OUT_DELAY);
    }

    void hide() {
        view.setVisibility(View.INVISIBLE);
        view.setAlpha(0f);
    }

    private void fadeIn() {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(FADE_IN_DURATION);
    }

    private void fadeOut() {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(0f)
                .setDuration(FADE_OUT_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // ignore
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // ignore
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // ignore
                    }
                });
    }

    private static final int FADE_IN_DELAY = 10;
    private static final int FADE_IN_DURATION = 500;
    private static final int FADE_OUT_DELAY = 300;
    private static final int FADE_OUT_DURATION = 500;

}
