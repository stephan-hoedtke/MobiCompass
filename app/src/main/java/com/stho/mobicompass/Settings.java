package com.stho.mobicompass;

import java.util.Set;

final class Settings {
    final boolean showMagnetometer;
    final boolean showAccelerometer;

    Settings(boolean showMagnetometer, boolean showAccelerometer) {
        this.showMagnetometer = showMagnetometer;
        this.showAccelerometer = showAccelerometer;
    }

    static Settings defaultValue() {
        return new Settings(false, false);
    }
}

