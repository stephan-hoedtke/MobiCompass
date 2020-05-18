package com.stho.mobicompass;

import android.os.SystemClock;

class LowPassFilter {

    private final Vector gravity = new Vector();
    private long startTimeNanos = 0;
    private long count = 0;

    LowPassFilter() {
        setAcceleration(new float[]{0f, 0f, 9.78f});
    }

    Vector setAcceleration(float[] acceleration) {
        float dt = getAverageTimeDifferenceInSeconds();
        lowPassFilter(acceleration, dt);
        return gravity;
    }

    private static final float TIME_CONSTANT = 0.2f;

    private void lowPassFilter(float[] acceleration, float dt) {
        if (dt > 0) {
            float alpha = dt / (TIME_CONSTANT + dt);
            gravity.x += alpha * (acceleration[0] - gravity.x);
            gravity.y += alpha * (acceleration[1] - gravity.y);
            gravity.z += alpha * (acceleration[2] - gravity.z);
        } else {
            gravity.setValues(acceleration);
        }
    }

    private static final float NANOS_PER_SECOND = 1000000000f;

    private float getAverageTimeDifferenceInSeconds() {
        if (count < 1) {
            startTimeNanos = SystemClock.elapsedRealtimeNanos();
            count++;
            return 0;
        }
        else {
            long averageNanos = (SystemClock.elapsedRealtimeNanos() - startTimeNanos) / count++;
            return averageNanos / NANOS_PER_SECOND;
        }
    }
}
