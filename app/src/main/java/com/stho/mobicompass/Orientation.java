package com.stho.mobicompass;



/**
 * Orientation: angles in degree (azimuth, pitch, roll)
 *
 * Project the positive y-axis [from the bottom edge to the top edge of the phone] to the sphere
 *      - azimuth: the angle to the geographic north at the horizon plane
 *      - pitch: the angle downwards when the top edge is tilted down
 *      - roll: the angle downwards when the left edge is tilted down
 *
 * Project the negative z-axis [how your eyes look into the phone] to the spheres
 *      - center azimuth: the angle to the geographic north at the horizon plane
 *      - center altitude: the angle upwards or downwards from the horizon
 *
 * Pitch (around X axis):
 *   When the device is placed face up on a table, the pitch value is 0.
 *   When the positive Z axis begins to tilt towards the positive Y axis, the pitch angle becomes positive.
 *   (This is when the top edge of the device is moving downwards)
 *   The value of Pitch ranges from -180 degrees to 180 degrees.
 *
 * Roll (around Y axis):
 *   When the device is placed face up on a table, the roll value is 0.
 *   When the positive X axis begins to tilt towards the positive Z axis, the roll angle becomes positive.
 *   (This is when the left edge of the device is moving downwards)
 *   The value of Roll ranges from -90 degrees to 90 degrees.
 *
 * Azimuth (around Z axis):
 *   The following table shows the value of Azimuth when the positive Y axis of the device is aligned to the magnetic north, south, east, and west
 *      North -> 0
 *      East -> 90
 *      South -> 180
 *      West -> 270
 *
 * Altitude (top edge of the device pointing upwards) is the opposite of pitch (top edge of the device pointing downwards)
 */
@SuppressWarnings("unused")
class Orientation {
    private final double azimuth;
    private final double pitch;
    private final double roll;
    private final double centerAzimuth;
    private final double centerAltitude;

    Orientation(double azimuth, double pitch, double roll, double centerAzimuth, double centerAltitude) {
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
        this.centerAzimuth = centerAzimuth;
        this.centerAltitude = centerAltitude;
    }

    double getAzimuth() { return azimuth; }
    double getPitch() { return pitch; }
    double getRoll() { return roll; }
    double getCenterAzimuth() { return centerAzimuth; }
    double getCenterAltitude() { return centerAltitude; }
    double getAltitude() { return -pitch; }

    Orientation normalize() {
        if (roll < -90 || roll > 90) {
            return new Orientation(
                    azimuth + 180,
                    180 - pitch,
                    roll - 180,
                    centerAzimuth,
                    centerAltitude
            );
        } else {
            return this;
        }
    }
}


