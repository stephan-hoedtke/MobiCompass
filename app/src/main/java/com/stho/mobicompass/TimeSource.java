package com.stho.mobicompass;

import android.os.SystemClock;


public class TimeSource {

    public double getElapsedRealtimeSeconds() {
        return SECONDS_PER_NANOSECOND * SystemClock.elapsedRealtimeNanos();
    }

    private static final double SECONDS_PER_NANOSECOND = 1.0 / 1000000000.0;
}


