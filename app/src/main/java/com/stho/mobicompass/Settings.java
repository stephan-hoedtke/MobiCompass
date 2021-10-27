package com.stho.mobicompass;

import java.util.Set;

final class Settings {
    final boolean showMagnetometer;
    final boolean showAccelerometer;
    final boolean applyLowPassFilter;

    Settings(boolean showMagnetometer, boolean showAccelerometer, boolean applyLowPassFilter) {
        this.showMagnetometer = showMagnetometer;
        this.showAccelerometer = showAccelerometer;
        this.applyLowPassFilter = applyLowPassFilter;
    }

    static Settings defaultValue() {
        return new Settings(false, false, true);
    }
}

