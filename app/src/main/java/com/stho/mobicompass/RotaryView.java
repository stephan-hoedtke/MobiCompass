package com.stho.mobicompass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class RotaryView extends AppCompatImageView {

    public interface OnRotateListener {
        void onRotate(double delta);
    }

    public interface OnDoubleTapListener {
        void onDoubleTap();
    }

    private OnRotateListener rotateListener;
    private OnDoubleTapListener doubleTapListener;
    private double previousAngle = 0;
    private GestureDetector gestureDetector;

    public RotaryView(Context context) {
        super(context);
        setupGestureDetector();
        setImageResource(R.drawable.magnetic_compass_ring);
    }

    public RotaryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupGestureDetector();
        setImageResource(R.drawable.magnetic_compass_ring);
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                RotaryView.this.onDoubleTap();
                return super.onDoubleTap(e);
            }
        });
    }

    public void rotate(double delta) {
        float angle = getRotation();
        angle += delta;
        setRotation(angle);
        onRotate(delta);
    }

    private void onRotate(double delta) {
        if (rotateListener != null)
            rotateListener.onRotate(delta);
    }

    private void onDoubleTap() {
        if (doubleTapListener != null)
            doubleTapListener.onDoubleTap();
    }

    public void setOnRotateListener(OnRotateListener listener) {
        this.rotateListener = listener;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener listener) {
        this.doubleTapListener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        onTouchEventRotationDragHandler(event);
        return true;
    }

    public void onTouchEventRotationDragHandler(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                previousAngle = getRotation() + getAngle(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                final double alpha = getRotation() + getAngle(event.getX(), event.getY());
                final double delta = ensureAngleRange(alpha - previousAngle);
                previousAngle = alpha;
                rotate(delta);
                break;
        }
    }

    private double getAngle(float x, float y) {
        float cx = getWidth() >> 1;
        float cy = getHeight() >> 1;
        return Math.atan2(y - cy, x - cx) * 180 / Math.PI + 90;
    }

    private static double ensureAngleRange(double delta) {
        while (delta > 180) {
            delta -= 360;
        }
        while (delta < -180) {
            delta += 360;
        }
        return delta;
    }
}
