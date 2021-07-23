package com.stho.mobicompass;

/**
 * Created by shoedtke on 11.10.2016.
 */
@SuppressWarnings("WeakerAccess")
public class Degree {

    public static float normalize(float x) {
        x = x % 360;
        while (x > 360)
            x -= 360;
        while (x < 0)
            x += 360;
        return x;
    }

    public static double normalize(double x) {
        x = x % 360;
        while (x > 360)
            x -= 360;
        while (x < 0)
            x += 360;
        return x;
    }

    public static float normalizePlusMinus(float x) {
        x = x % 360;
        while (x > 180)
            x -= 360;
        while (x < -180)
            x += 360;
        return x;
    }

    public static double normalizePlusMinus(double x) {
        x = x % 360;
        while (x > 180)
            x -= 360;
        while (x < -180)
            x += 360;
        return x;
    }

    public static float getAngleDifference(float from, float to) {
        return normalizePlusMinus(to - from);
    }

    public static double sin(double degree) {
        return Math.sin(Math.toRadians(degree));
    }

    public static double cos(double degree) {
        return Math.cos(Math.toRadians(degree));
    }

    public static double tan(double degree) {
        return Math.tan(Math.toRadians(degree));
    }

    public static double arcTan2(double y, double x) {
        return Math.toDegrees(Math.atan2(y, x));
    }

    public static double arcSin(double x) {
        return Math.toDegrees(Math.asin(x));
    }

    public static double arcCos(double x) {
        return Math.toDegrees(Math.acos(x));
    }


}

