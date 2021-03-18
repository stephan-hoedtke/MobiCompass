package com.stho.mobicompass;

import android.os.SystemClock;


class Acceleration {

    // x(t) = (x0 + (v0 + x0 * delta) * t) * EXP(-delta * t)
    // x(0) = x0
    // x(1) = x0 * xsi
    // v(0) = v0
    // v(t) = (v0 - (v0 + s0 * delta) * delta * t) * EXP(-delta * t)
    //      gamma := v0 / s0
    // -->  delta = -1 - gamma - W(- xsi / EXP(1 + gamma)
    private double x0;
    private double v0;
    private double delta;
    private long t0;
    private double alpha;
    private final double factor;

    private static final double NANOSECONDS_PER_SECOND = 1000000000;

    Acceleration() {
        factor = 1.1 / NANOSECONDS_PER_SECOND;
        alpha = 0;
        delta = 7.0;
        x0 = 0;
        v0 = 0;
        t0 = SystemClock.elapsedRealtimeNanos();
    }

    double getPosition() {
        double t = getTime(SystemClock.elapsedRealtimeNanos());
        return getPosition(t);
    }

    /*
        for angles in Radiant (0 < angle < 2 PI)
     */
    void rotateTo(double targetAngle) {
        long elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        double t = getTime(elapsedRealtimeNanos);
        double v = getSpeed(t);
        double s = getPosition(t);
        x0 = Angle.getAngleDifferenceFromToRadiant(s, targetAngle);
        v0 = v;
        t0 = elapsedRealtimeNanos;
        alpha = targetAngle;
    }


    private double getTime(long elapsedRealtimeNanos) {
        long nanos = elapsedRealtimeNanos - t0;
        return factor * nanos;
    }

    private double getPosition(double t) {
        double x = getX(t);
        return Angle.normalize(alpha - x);
    }

    private double getSpeed(double t) {
        return getV(t);
    }

    private double getX(double t) {
        if (t < 0) return x0;
        if (t > 2) return 0;
        return (x0 + (x0 * delta + v0) * t) * Math.exp(-delta * t);
    }

    private double getV(double t) {
        if (t < 0) return v0;
        if (t > 2) return 0;
        return (v0 - (v0 + x0 * delta) * delta * t) * Math.exp(-delta * t);
    }
}
