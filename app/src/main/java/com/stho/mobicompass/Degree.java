package com.stho.mobicompass;

/**
 * Created by shoedtke on 11.10.2016.
 */
public class Degree {

    public static double normalize(double x) {
        x = x % 360;
        while (x > 360)
            x -= 360;
        while (x < 0)
            x += 360;
        return x;
    }

    public static double arcTan2(double y, double x) {
        return Math.toDegrees(Math.atan2(y, x));
    }

    public static double arcSin(double x) {
        return Math.toDegrees(Math.asin(x));
    }
}

