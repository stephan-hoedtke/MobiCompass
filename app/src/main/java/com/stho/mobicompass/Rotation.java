package com.stho.mobicompass;

public class Rotation {

    /**
     * Returns the orientation (azimuth, pitch, roll, center azimuth, center altitude) of the device
     *      for a rotation from sensor frame into earth frame
     *
     *      (C) The center "pointer" vector is defined by
     *              C = M * (0, 0, -1)
     *
     *          --> C = (-m13, -m23, -m33)
     *
     *              center azimuth  = atan2(-m13, -m23)
     *              center azimuth = asin(-m33) // opposite of pitch
     */
    public static Orientation getOrientationFor(RotationMatrix m) {
        double m12 = m.m12;
        double m13 = m.m13;
        double m21 = m.m21;
        double m22 = m.m22;
        double m23 = m.m23;
        double m31 = m.m31;
        double m32 = m.m32;
        double m33 = m.m33;
        if (isGimbalLockForSinus(m32)) {
            if (m32 < 0) { // pitch 90°
                double roll = Degree.arcTan2(m21, m23);
                return new Orientation(
                        0.0,
                        90.0,
                        roll,
                        180 - roll,
                        0.0);
            } else { // pitch -90°
                double roll = Degree.arcTan2(-m21, -m23);
                return new Orientation(
                        0.0,
                        -90.0,
                        roll,
                        roll,
                        0.0);
            }
        } else {
            if (isGimbalLockForCenter(m13, m23)) { // pitch 0°
                double azimuth = Degree.arcTan2(m12, m22);
                double roll = Degree.arcTan2(m31, m33);
                return new Orientation(
                        azimuth,
                        Degree.arcSin(-m32),
                        roll,
                        azimuth,
                        roll - 90);
            } else {
                return new Orientation(
                        Degree.arcTan2(m12, m22),
                        Degree.arcSin(-m32),
                        Degree.arcTan2(m31, m33),
                        Degree.arcTan2(-m13, -m23),
                        Degree.arcSin(-m33));
            }
        }
    }

    private static final double FREE_FALL_GRAVITY_SQUARED = 0.01 * 9.81 * 9.81;
    private static final double FREE_FALL_MAGNETOMETER_SQUARED = 0.01;

    public static RotationMatrix getRotationMatrixFromAccelerationMagnetometer(Vector acceleration, Vector magnetometer, RotationMatrix defaultValue) {
        RotationMatrix matrix = getRotationMatrixFromAccelerationMagnetometer(acceleration, magnetometer);
        return (matrix == null) ? defaultValue : matrix;
    }

    /**
     * Returns the rotation matrix M for a device defined by the gravity and the geomagnetic vector
     *      in case of free fall or close to magnetic pole it returns null
     *      as the rotation matrix cannot be calculated
     *
     * @param acceleration acceleration (a vector pointing upwards in default position)
     * @param magnetometer magnetic (a vector pointing down to the magnetic north)
     *
     * see also: SensorManager.getRotationMatrix()
     */
    public static RotationMatrix getRotationMatrixFromAccelerationMagnetometer(Vector acceleration, Vector magnetometer) {
        double normSquareA = acceleration.normSquare();
        if (normSquareA < FREE_FALL_GRAVITY_SQUARED) {
            // free fall detected, typical values of gravity should be 9.81
            return null;
        }

        Vector H = magnetometer.cross(acceleration);
        double normSquareH = H.normSquare();
        if (normSquareH < FREE_FALL_MAGNETOMETER_SQUARED) {
            // free fall or in space or close to magnetic north pole, typical values of magnetometer should be more than 100
            return null;
        }

        Vector h = H.div(Math.sqrt(normSquareH));
        Vector a = acceleration.div(Math.sqrt(normSquareA));
        Vector m = a.cross(h);
        return new RotationMatrix(
                h.x, h.y, h.z,
                m.x, m.y, m.z,
                a.x, a.y, a.z);
    }

    /**
     * Returns the rotation matrix as integration of angle velocity from gyroscope of a time period
     *
     * @param omega angle velocity around x, y, z, in radians/second
     * @param dt time period in seconds
     */
    public static Quaternion getRotationFromGyro(Vector omega, double dt) {
        // Calculate the angular speed of the sample
        double omegaMagnitude = omega.norm();

        // Normalize the rotation vector if it's big enough to get the axis
        // (that is, EPSILON should represent your maximum allowable margin of error)
        Vector w = (omegaMagnitude > OMEGA_THRESHOLD) ? omega.div(omegaMagnitude) : omega;

        // Quaternion integration:
        // ds/dt = omega x s
        // with s = q # s0 # q* follows
        //      dq/dt = 0.5 * omega # q
        //      q(t) = exp(0.5 * omega * (t - t0)) # q0
        //      q(t) = cos(|v|) + v / |v| * sin(|v|) # q0 with v = 0.5 * omega * (t - t0)
        //      this is equivalent to a rotation by theta around the rotation vector omega/|omega| with theta = |omega| * (t - t0)
        double theta = omegaMagnitude * dt;
        return Quaternion.forRotation(w, theta);
    }


    /**
     * Returns if sin(x) is about +/- 1.0
     */
    private static boolean isGimbalLockForSinus(double sinX) {
        return (sinX < GIMBAL_LOCK_SINUS_MINIMUM) || (sinX > GIMBAL_LOCK_SINUS_MAXIMUM);
    }

    /**
     * Returns if x^2 +y^2 is too small to calculate the atan2
     */
    private static boolean isGimbalLockForCenter(double sinX, double cosX) {
        return (Math.abs(sinX) < GIMBAL_LOCK_SINUS_TOLERANCE) && (Math.abs(cosX) < GIMBAL_LOCK_SINUS_TOLERANCE);
    }

    /**
     * When the pitch is about 90° (Gimbal lock) the rounding errors of x, y, z produce unstable azimuth and roll
     *      pitch = +/- 90°
     *      --> z = +/- 1.0
     *          x = +/- 0.0
     *          y = +/- 0.0
     *      --> atan2(...,...) can be anything.
     *
     * Tolerance estimation:
     *      x,y < 0.001 --> z > sqrt(1 - x * x - y * y) = sqrt(0.999998) = 0.999999 --> 89.92°
     *          pitch = +/- (90° +/- 0.08°) or
     *          pitch = +/- (PI/2 +/- 0.001414) or
     *          sin(x) = +/- (1.0 +/- 0.000001)
     *
     */
    private static final double GIMBAL_LOCK_SINUS_TOLERANCE = 0.000001;
    private static final double GIMBAL_LOCK_SINUS_MINIMUM = -0.999999;
    private static final double GIMBAL_LOCK_SINUS_MAXIMUM = 0.999999;
    private static final double OMEGA_THRESHOLD = 0.0000001;
}
