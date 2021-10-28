package com.stho.mobicompass;

import android.util.Log;

import java.util.Date;

public class FastAHRSFilter {

    /**
     * Update the current estimation by a correction derived from the normalized sensor accelerometer and magnetometer readings a and m
     */
    public static Quaternion update(Vector a, Vector m, Vector omega, double dt, Quaternion estimate) {

        // Get updated Gyro delta rotation from gyroscope readings
        Quaternion deltaRotation = getDeltaRotationFromGyroFSCF(omega, dt);

        // prediction := estimation rotated by gyroscope readings
        Quaternion prediction = estimate.times(deltaRotation);
        RotationMatrix matrix = prediction.toRotationMatrix();

        // prediction of a := Vector(0.0, 0.0, 1.0).rotateBy(prediction.inverse()) --> normalized
        Vector aPrediction = new Vector(
                matrix.m31,
                matrix.m32,
                matrix.m33);

        // reference direction of magnetic field in earth frame after distortion compensation
        Vector b = flux(aPrediction, m);

        // prediction of m := Vector(0.0, b.y, b.z).rotateBy(prediction.inverse()) --> normalized
        Vector mPrediction = new Vector(
                matrix.m21 * b.y + matrix.m31 * b.z,
                matrix.m22 * b.y + matrix.m32 * b.z,
                matrix.m23 * b.y + matrix.m33 * b.z);

        // Calculate the required correction:
        // - the magnitude as "angle between prediction and measurement" := arcCos(measurement dot prediction)
        // - the direction as unit vector := (measurement x prediction).normalize()
        double aAlpha = angleBetweenUnitVectors(a, aPrediction);
        Vector aCorrection = a.cross(aPrediction).normalize();

        double mAlpha = angleBetweenUnitVectors(m, mPrediction);
        Vector mCorrection = m.cross(mPrediction).normalize(EPS);

        // Limit the magnitude of correction with time factor
        double aBeta = Math.min(dt, 1.0) * f(aAlpha);
        double mBeta = Math.min(dt, 1.0) * f(mAlpha);

        // Calculate fused correction and the new estimate
        Vector fCorrection = getFusedCorrectionFSCF(aCorrection, aBeta, mCorrection, mBeta);
        Log.d("FSCF",Formatter.toString(new Date()) +
                        ", a=" + Formatter.toString(aAlpha) +
                        ", m=" + Formatter.toString(mAlpha) +
                        ", correction=" + fCorrection.toString()
                );

        double fNorm = fCorrection.norm();
        if (fNorm > EPS) {
            // new estimate := prediction rotate by fused correction from acceleration and magnetometer
            Quaternion qCorrection = new Quaternion(fCorrection.x, fCorrection.y, fCorrection.z, 1.0);
            return prediction.times(qCorrection).normalize();
        } else {
            // new estimate := prediction
            return prediction;
        }
    }

    /**
     * The rotation is not exactly the same, but similar to the "default approach":
     *      theta = ||omega|| * dt
     *      Q = Q(s = cos(theta/2), v = sin(theta2) * |omega|)
     *
     * @param omega angle velocity around x, y, z, in radians/second
     * @param dt time period in seconds
     */
    private static Quaternion getDeltaRotationFromGyroFSCF(Vector omega, double dt) {
        double dx = omega.x * dt;
        double dy = omega.y * dt;
        double dz = omega.z * dt;
        return new Quaternion(dx / 2, dy / 2, dz / 2, 1.0);
    }

    private static double angleBetweenUnitVectors(Vector a, Vector b) {
        double c = a.dot(b);
        return (-1.0 <= c && c <= 1.0) ? Math.acos(c) : 0.0;
    }

    /**
     * Default:
     *      f(x) := lambda * x
     *              with lambda = 0.5 for small x
     * Non-linear:
     *      f(x) := lambda * x + tau * x^2
     *              with f(x) = lambda * x for small x
     *               and f(x) = 10 * lambda * x for x about 20°
     *
     *              20° --> x = 2 PI / 360° * 20° = PI/9
     *              f'(0) = lambda
     *              f(0) = 0
     *              f(PI/9) = 10 * lambda * PI/9 = lambda * PI/9 + tau * (PI/9)^2
     *              tau = lambda * 81 / PI
     */
    private static double f(double alpha) {
        return Math.min(alpha * (LAMBDA1 + alpha * TAU1), LAMBDA2);
    }

    /**
     * Returns the magnetic field in earth frame after distortion correction.
     *
     * Note:
     * Normalization is required as the input vectors may not be normalized and the calculation may fail badly otherwise.
     */
    private static Vector flux(Vector a, Vector m) {
        double bz = (a.x * m.x + a.y * m.y + a.z * m.z) / (a.norm() * m.norm());
        double by = Math.sqrt(1 - bz * bz);
        return new Vector(0.0, by, bz);
    }

    private static Vector getFusedCorrectionFSCF(Vector a, double aBeta, Vector m, double mBeta) {
        double fa = aBeta / 2;
        double fm = mBeta / 2;
        return new Vector(
                a.x * fa + m.x * fm,
                a.y * fa + m.y * fm,
                a.z * fa + m.z * fm);
    }

    private static final double EPS = 1E-9;

    static final double LAMBDA1 = 1.0;
    static final double LAMBDA2 = 1.0;

    static final double TAU1 = 81 / Math.PI * LAMBDA1;
}
