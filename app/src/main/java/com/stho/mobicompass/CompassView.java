package com.stho.mobicompass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * Drawing
 * - the outer ring (fixed)
 * - the inner ring with N, NE, E, SE, S, SW, W, NW markers (rotated by ringAngle)
 * - the north pointer (rotated by northPointerAngle)
 */
public class CompassView extends View {

    public interface OnRotateListener {
        void onRotate(double delta);
    }

    public interface OnDoubleTapListener {
        void onDoubleTap();
    }

    private OnRotateListener rotateListener = null;
    private OnDoubleTapListener doubleTapListener = null;
    private double previousAngle = 0;
    private double northPointerAngle = 15.0;
    private double ringAngle = 30.0;
    private Bitmap ring = null;
    private Bitmap background = null;
    private Bitmap northPointer = null;
    private GestureDetector gestureDetector = null;
    private Matrix matrix = null;

    public CompassView(Context context) {
        super(context);
        setupGestureDetector();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupGestureDetector();
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                CompassView.this.onDoubleTap();
                return super.onDoubleTap(e);
            }
        });
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

    public void setNorthPointerAngle(double newAngle) {
        northPointerAngle = newAngle;
        invalidate();
    }

    public void setRingAngle(double newAngle) {
        ringAngle = newAngle;
        invalidate();
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
                previousAngle = getAngle(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                final double alpha = getAngle(event.getX(), event.getY());
                final double delta = ensureAngleInRange(alpha - previousAngle);
                previousAngle = alpha;
                onRotate(delta);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (matrix == null) {
            onCreate(getContext());
        }

        /*
            Mind, all three bitmaps:
             - background,
             - ring,
             - northPointer
            are of the same size in pixels: size x size (originally and after scaling)
         */

        float px = getWidth() >> 1;
        float py = getHeight() >> 1;
        float dx = Math.round(px - (background.getHeight() >> 1));
        float dy = Math.round(py - (background.getHeight() >> 1));

        matrix.setTranslate(dx, dy);
        matrix.postRotate((float)ringAngle, px, py);
        canvas.drawBitmap(ring, matrix, null);

        matrix.setTranslate(dx, dy);
        canvas.drawBitmap(background, matrix, null);

        matrix.setTranslate(dx, dy);
        matrix.postRotate((float)northPointerAngle, px, py);
        canvas.drawBitmap(northPointer, matrix, null);
    }

    private void onCreate(Context context) {
        int size = Math.min(getWidth(), getHeight());
        ring = createBitmap(context, R.drawable.magnetic_compass_ring, size);
        background = createBitmap(context, R.drawable.magnetic_compass_background, size);
        northPointer = createBitmap(context, R.drawable.magnetic_compass_pointer, size);
        matrix = new Matrix();

    }

    private static Bitmap createBitmap(Context context, int resourceId, int size) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        return Bitmap.createScaledBitmap(bitmap, size, size, false);
    }

    private double getAngle(float x, float y) {
        float cx = getWidth() >> 1;
        float cy = getHeight() >> 1;
        return Math.atan2(y - cy, x - cx) * 180 / Math.PI + 90;
    }

    private static double ensureAngleInRange(double delta) {
        while (delta > 180) {
            delta -= 360;
        }
        while (delta < -180) {
            delta += 360;
        }
        return delta;
    }
}
