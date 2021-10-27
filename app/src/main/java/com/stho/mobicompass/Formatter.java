package com.stho.mobicompass;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Formatter {
    private static final DecimalFormat fm = new DecimalFormat("0.0000000000");
    private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);

    static String toString(float f) {
        return fm.format(f);
    }

    static String toString(double f) {
        return fm.format(f);
    }

    static String toString(Date d) {
        return df.format(d);
    }
}
