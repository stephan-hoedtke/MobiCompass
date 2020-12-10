package com.stho.mobicompass;

import android.os.SystemClock;

class Acceleration {

    // s(t) = a * t^3 + b * t^2 + c * t + d
    // s(0) = s0
    // s(1) = s1
    // s'(0) = v0
    // s'(1) = 0
    // -->  a = v0 - 2 (s1 - s0)
    //      b = 3 (s1 - s0) - 2 v0
    //      c = v0
    //      d = s0
    private double v0;
    private double s0;
    private double s1;
    private double a;
    private double b;
    private double c;
    private double d;
    private long t0;
    private final double factor;

    private static final double NANOSECONDS_PER_SECOND = 1000000000;

    Acceleration() {
        factor = 1.1 / NANOSECONDS_PER_SECOND;
        v0 = 0;
        s0 = 0;
        s1 = 0;
        t0 = SystemClock.elapsedRealtimeNanos();
        calculateFormula();
    }

    double getPosition() {
        double t = getTime(SystemClock.elapsedRealtimeNanos());
        return getPosition(t);
    }

    void update(double targetPosition) {
        long elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        double t = getTime(elapsedRealtimeNanos);
        double v = getSpeed(t);
        double s = getPosition(t);
        v0 = v;
        s0 = s;
        s1 = targetPosition;
        t0 = elapsedRealtimeNanos;
        calculateFormula();
    }

    /*
        for angles in Radiant (0 < angle < 2 PI)
     */
    void rotateTo(double targetAngle) {
        long elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        double t = getTime(elapsedRealtimeNanos);
        double v = getSpeed(t);
        double s = getPosition(t);
        double angle = Angle.getAngleDifferenceFromToRadiant(s, targetAngle);
        v0 = v;
        s0 = s;
        s1 = s + angle;
        t0 = elapsedRealtimeNanos;
        if (angle > 0 && s1 > TWO_PI) {
            s0 -= TWO_PI;
            s1 -= TWO_PI;
        }
        else if (angle < 0 && s1 < 0) {
            s0 += TWO_PI;
            s1 += TWO_PI;
        }
        calculateFormula();
    }


    private double getTime(long elapsedRealtimeNanos) {
        long nanos = elapsedRealtimeNanos - t0;
        return factor * nanos;
    }

    private void calculateFormula() {
        a = v0 - 2 * (s1 - s0);
        b = 3 * (s1 - s0) - 2 * v0;
        c = v0;
        d = s0;
    }

    private double getPosition(double t) {
        if (t < 0) return s0;
        if (t > 1) return s1;
        return ((a * t + b) * t + c) * t + d;
    }

    private double getSpeed(double t) {
        if (t < 0) return v0;
        if (t > 1) return 0;
        return (3 * a * t + 2 * b) * t + c;
    }

    private static final double TWO_PI = 2 * Math.PI;
}
