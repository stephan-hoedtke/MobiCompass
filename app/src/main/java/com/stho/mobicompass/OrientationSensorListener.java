package com.stho.mobicompass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class OrientationSensorListener implements SensorEventListener {

    interface IOrientationFilter {
        void onOrientationAnglesChanged(Quaternion orientation);
        Orientation getCurrentOrientation();

    }
    private final IOrientationFilter filter;
    private final SensorManager sensorManager;
    private final WindowManager windowManager;

    public OrientationSensorListener(Context context, IOrientationFilter filter) {
        this.filter = filter;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] gyroscopeReading = new float[3];
    private final Timer timer = new Timer();

    private boolean hasAccelerometer = false;
    private boolean hasMagnetometer = false;
    private boolean hasEstimate = false;
    private Quaternion estimate = Quaternion.defaultValue();
    private Display display;

    public void onResume() {
        // TODO: for API30 you shall user: context.display.rotation
        display = windowManager.getDefaultDisplay();
        hasAccelerometer = false;
        hasMagnetometer = false;
        hasEstimate = false;
        initializeRotationVectorSensor();
    }

    public void onPause() {
        display = null;
        removeSensorListeners();
    }

    /**
     * The rotation vector sensor is fusing gyroscope, accelerometer and magnetometer. No further sensor fusion required.
     */
    private void initializeRotationVectorSensor() {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);

        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);

        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);
    }

    private void removeSensorListeners() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // we don't care
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
                hasAccelerometer = true;
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
                hasMagnetometer = true;
                break;

            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values, 0, gyroscopeReading, 0, gyroscopeReading.length);
                updateOrientationAnglesFromGyroscope();
                break;
        }
    }

    // Compute the orientation based on the most recent readings from the device's accelerometer and magnetometer.
    private Quaternion getOrientationFromAccelerometerMagnetometer() {
        if (!hasAccelerometer) {
            return null;
        }
        if (!hasMagnetometer) {
            return null;
        }
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            RotationMatrix matrix = RotationMatrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix));
            return Quaternion.fromRotationMatrix(matrix);
        }
        return null;
    }

    /*
      See the following training materials from google.
      https://codelabs.developers.google.com/codelabs/advanced-android-training-sensor-orientation/index.html?index=..%2F..advanced-android-training#0
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private float[] getAdjustedRotationMatrix(float[] rotationMatrix) {
        float[] rotationMatrixAdjusted = new float[9];
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                return rotationMatrix;

            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMatrixAdjusted);
                return rotationMatrixAdjusted;

            case Surface.ROTATION_180:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, rotationMatrixAdjusted);
                return rotationMatrixAdjusted;

            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, rotationMatrixAdjusted);
                return rotationMatrixAdjusted;
        }
        return rotationMatrix;
    }

    private void updateOrientationAnglesFromGyroscope() {
        if (!hasAccelerometer) {
            return;
        }
        if (!hasMagnetometer) {
            return;
        }
        if (!hasEstimate) {
            Quaternion orientation = getOrientationFromAccelerometerMagnetometer();
            if (orientation == null) {
                return;
            }
            estimate = orientation;
            hasEstimate = true;
        }

        double dt = timer.getNextTime();
        if (dt > 0) {
            filterUpdate(dt);
            filter.onOrientationAnglesChanged(estimate);
        }
    }

    /**
     * Fast AHRS Filter for Accelerometer, Magnetometer, and Gyroscope Combination with Separated Sensor Corrections
     *      by Josef Justa, Vaclav Smidl, Alex Hamacek, April 2020
     */
    private void filterUpdate(double dt) {

        Vector a = Vector.fromFloatArray(accelerometerReading).normalize();
        Vector m = Vector.fromFloatArray(magnetometerReading).normalize();
        Vector omega = Vector.fromFloatArray(gyroscopeReading);

        // Get updated Gyro delta rotation from gyroscope readings
        Quaternion deltaRotation = Rotation.getRotationFromGyroFSCF(omega, dt);

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

        double aAlpha = Math.acos(a.dot(aPrediction));
        Vector aCorrectionRaw = a.cross(aPrediction);
        double aNorm = aCorrectionRaw.norm();
        Vector aCorrection = (aNorm > EPS) ? aCorrectionRaw.div(aNorm) : Vector.defaultValue();

        double mAlpha = Math.acos(m.dot(mPrediction));
        Vector mCorrectionRaw = m.cross(mPrediction);
        double mNorm = mCorrectionRaw.norm();
        Vector mCorrection = (mNorm > EPS) ? mCorrectionRaw.div(mNorm) : Vector.defaultValue();

        double aBeta = 0.5 * Math.min(aAlpha * LAMBDA1, LAMBDA2);
        double mBeta = 0.5 * Math.min(mAlpha * LAMBDA1, LAMBDA2);

        Vector fCorrection = new Vector(
                aCorrection.x * aBeta + mCorrection.x * mBeta,
                aCorrection.y * aBeta + mCorrection.y * mBeta,
                aCorrection.z * aBeta + mCorrection.z * mBeta);

        double fNorm = fCorrection.norm();
        if (fNorm > EPS) {
            Quaternion qCorrection = new Quaternion(fCorrection.x, fCorrection.y, fCorrection.z, 1.0);
            estimate = prediction.times(qCorrection).normalize();
        }
        else {
            estimate = prediction;
        }
    }

    private static final double EPS = 0.0000001;
    private static final double LAMBDA1 = 0.005;
    private static final double LAMBDA2 = 0.035;

    /**
     * Returns the magnetic field in earth frame after distortion correction
     */
    private static Vector flux(Vector a, Vector m) {
        double bz = (a.x * m.x + a.y * m.y + a.z * m.z) / (a.norm() * m.norm());
        double by = Math.sqrt(1 - bz * bz);
        return new Vector(0.0, by, bz);
    }
}
