package com.stho.mobicompass;

final class Settings {
    final boolean showMagnetometer;
    final boolean showAccelerometer;
    final boolean applyLowPassFilter;

    Settings(boolean showMagnetometer, boolean showAccelerometer, boolean applyLowPassFilter) {
        this.showMagnetometer = showMagnetometer;
        this.showAccelerometer = showAccelerometer;
        this.applyLowPassFilter = applyLowPassFilter;
    }

    boolean isModified() {
        return showMagnetometer || showAccelerometer || !applyLowPassFilter;
    }

    static Settings defaultValue() {
        return new Settings(false, false, true);
    }
}

