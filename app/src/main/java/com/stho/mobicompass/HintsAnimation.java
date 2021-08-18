package com.stho.mobicompass;

import android.animation.Animator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class HintsAnimation {
    private final View view;
    private final Handler handler = new Handler(Looper.getMainLooper());

    static HintsAnimation build(View view) {
        return new HintsAnimation(view);
    }

    void cleanup() {
        handler.removeCallbacksAndMessages(null);
    }

    void show() {
        fadeIn();
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::fadeOut, FADE_OUT_TIMEOUT);
    }

    void dismiss() {
        handler.removeCallbacksAndMessages(null);
        fadeOut();
    }

    void hide() {
        view.setVisibility(View.INVISIBLE);
        view.setAlpha(0f);
    }

    private HintsAnimation(View view) {
        this.view = view;
        hide();
    }

    private void fadeIn() {
        int dy = view.getHeight();
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(dy);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(FADE_IN_DURATION)
                .setListener(null);
    }


    private void fadeOut() {
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


    private static final int FADE_IN_DURATION = 500;
    private static final int FADE_OUT_DURATION = 500;
    private static final int FADE_OUT_TIMEOUT = 13000;
}
