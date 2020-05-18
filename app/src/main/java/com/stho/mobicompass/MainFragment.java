package com.stho.mobicompass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.stho.mobicompass.databinding.MainFragmentBinding;

public class MainFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private WindowManager windowManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] rotationMatrixAdjusted = new float[9];
    private final float[] orientationAngles = new float[3];
    private final Handler handler = new Handler();
    private MainViewModel viewModel;
    private MainFragmentBinding binding;
    private Display display;

    public static MainFragment build() {
        return new MainFragment();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = MainViewModel.build(this);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
        binding.compassRing.setOnAngleChangedListener(delta -> viewModel.rotate(delta));
        binding.compassRing.setOnDoubleTapListener(() -> viewModel.reset());
        binding.compassRing.setOnLongPressListener(() -> viewModel.seek());
        viewModel.getRingAngleLD().observe(getViewLifecycleOwner(), angle -> {
            binding.compassRing.setAngle(angle);
        });
        viewModel.getNorthPointerAngleLD().observe(getViewLifecycleOwner(), angle -> {
            binding.compassNorthPointer.setRotation(-angle);
            binding.headline.setText(Formatter.toString0(angle));
        });
        viewModel.getDirectionLD().observe(getViewLifecycleOwner(), direction -> binding.headline.setText(direction));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        display = windowManager.getDefaultDisplay();
        initializeAccelerationSensor();
        initializeMagneticFieldSensor();
        initializeHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeSensorListeners();
        removeHandler();
    }

    private static final int HANDLER_DELAY = 100;

    private void initializeAccelerationSensor() {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void initializeMagneticFieldSensor() {
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void initializeHandler() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewModel.updateNorthPointer();
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, HANDLER_DELAY);
    }

    private void removeHandler() {
        handler.removeCallbacksAndMessages(null);
    }

    private void removeSensorListeners() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
                updateOrientationAngles();
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
                updateOrientationAngles();
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // ignore
    }

    @SuppressWarnings("ConstantConditions")
    private NavController findNavController() {
        return Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private void updateOrientationAngles() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            SensorManager.getOrientation(getAdjustedRotationMatrix(), orientationAngles);
            viewModel.update(orientationAngles);
        }
    }

    /*
      See the following training materials from google.
      https://codelabs.developers.google.com/codelabs/advanced-android-training-sensor-orientation/index.html?index=..%2F..advanced-android-training#0
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private float[] getAdjustedRotationMatrix() {
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
}


