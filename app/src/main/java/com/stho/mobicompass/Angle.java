package com.stho.mobicompass;

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

    public static double normalizePlusMinus(double x) {
        x = x % TWO_PI;
        while (x > PI)
            x -= TWO_PI;
        while (x < -PI)
            x += TWO_PI;
        return x;
    }

    public static double getAngleDifference(double from, double to) {
        return normalizePlusMinus(to - from);
    }

    public static double rotateTo(double from, double to) {
        final double difference = getAngleDifference(from, to);
        return from + difference;
    }

    public static double toDegree(double x) {
        return x * 180 / Math.PI;
    }
}
