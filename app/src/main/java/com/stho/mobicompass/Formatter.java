package com.stho.mobicompass;

import java.util.Locale;

class Formatter {

    static String toString0(float value) {
        String sign = (value < 0) ? "-" : "+";
        return sign + String.format(Locale.ENGLISH, "%.0f", Math.abs(value));
    }


    static String toString2(float value) {
        String sign = (value < 0) ? "-" : "+";
        return sign + String.format(Locale.ENGLISH, "%.2f", Math.abs(value));
    }

    static String toString4(float value) {
        String sign = (value < 0) ? "-" : "+";
        return sign + String.format(Locale.ENGLISH, "%.4f", Math.abs(value));
    }

    static String toAngleString(float value) {
        String sign = (value < 0) ? "-" : "+";
        return sign + String.format(Locale.ENGLISH, "%.1f°", Math.abs(value * 180 / Math.PI));
    }
}
