package com.stho.mobicompass;

import android.animation.Animator;

public abstract class OnAnimationEndListener implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(Animator animation) {
        // ignore
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        // ignore
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        // ignore
    }
}
