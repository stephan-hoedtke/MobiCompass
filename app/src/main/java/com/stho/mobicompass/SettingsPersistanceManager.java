package com.stho.mobicompass;

import android.content.Context;
import android.content.SharedPreferences;

class SettingsPersistenceManager {

    private final Context context;

    SettingsPersistenceManager(Context context) {
        this.context = context;
    }

    Settings read() {
        SharedPreferences preferences = context.getSharedPreferences("MAIN", Context.MODE_PRIVATE);
        boolean showMagnetometer = preferences.getBoolean(SHOW_MAGNETOMETER, false);
        boolean showAccelerometer = preferences.getBoolean(SHOW_ACCELEROMETER, false);
        boolean applyLowPassFilter = preferences.getBoolean(APPLY_LOW_PASS_FILTER, true);
        return new Settings(showMagnetometer, showAccelerometer, applyLowPassFilter);
    }

    void save(Settings settings) {
        SharedPreferences preferences = context.getSharedPreferences("MAIN", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHOW_MAGNETOMETER, settings.showMagnetometer);
        editor.putBoolean(SHOW_ACCELEROMETER, settings.showAccelerometer);
        editor.putBoolean(APPLY_LOW_PASS_FILTER, settings.applyLowPassFilter);
        editor.apply();
    }

    private static final String SHOW_MAGNETOMETER = "SHOW_MAGNETOMETER";
    private static final String SHOW_ACCELEROMETER = "SHOW_ACCELEROMETER";
    private static final String APPLY_LOW_PASS_FILTER = "APPLY_LOW_PASS_FILTER";
}
