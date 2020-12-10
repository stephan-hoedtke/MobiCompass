package com.stho.mobicompass;

@SuppressWarnings("WeakerAccess")
public class Angle {

    private final static double PI = Math.PI;
    private final static double TWO_PI = 2 * Math.PI;

    public static double normalize(double x) {
        x = x % TWO_PI;
        while (x > TWO_PI)
            x -= TWO_PI;
        while (x < 0)
            x += TWO_PI;
        return x;
    }

    public static double normalizePlusMinusPI(double x) {
        x = x % TWO_PI;
        while (x > PI)
            x -= TWO_PI;
        while (x < -PI)
            x += TWO_PI;
        return x;
    }

    public static double getAngleDifferenceFromToRadiant(double from, double to) {
        return normalizePlusMinusPI(to - from);
    }

    public static float toDegree(double x) {
        return (float)(x * 180 / Math.PI);
    }
}
