package com.stho.mobicompass;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.stho.mobicompass.databinding.MainFragmentBinding;

public class MainFragment extends Fragment {

    private MainViewModel viewModel;
    private MainFragmentBinding binding;
    private OrientationSensorListener sensorListener;
    private ButtonAnimation buttonAnimation;
    private HintsAnimation hintsAnimation;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = MainViewModel.build(requireActivity());
        sensorListener = new OrientationSensorListener(requireContext(), viewModel.getOrientationFilter());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MainFragmentBinding.inflate(inflater, container, false);
        binding.compass.setOnRotateListener(delta -> viewModel.rotateRing(delta));
        binding.compass.setOnDoubleTapListener(() -> viewModel.fix());
        binding.buttonAutomaticMode.setOnClickListener(view -> viewModel.setAutomaticMode());
        binding.buttonShowHints.setOnClickListener(view -> displayHints());
        binding.buttonDismissHints.setOnClickListener(view -> dismissHints());
        binding.buttonSettings.setOnClickListener(view -> onSettings());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getRingAngleLD().observe(getViewLifecycleOwner(), this::observeRingAngle);
        viewModel.getNorthPointerPositionLD().observe(getViewLifecycleOwner(), this::observeNorthPointer);
        viewModel.getDirectionNameLD().observe(getViewLifecycleOwner(), this::observeDirection);
        viewModel.getManualModeLD().observe(getViewLifecycleOwner(), this::observeManualMode);
        viewModel.getLookAtPhoneFromAboveLD().observe(getViewLifecycleOwner(), this::getLookAtPhoneFromAboveLD);
        viewModel.getSettingsLD().observe(getViewLifecycleOwner(), this::onObserverSettings);
        buttonAnimation = ButtonAnimation.build(binding.buttonAutomaticMode);
        hintsAnimation = HintsAnimation.build(binding.hintsFrame);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorListener.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorListener.onPause();
        hintsAnimation.cleanup();
        buttonAnimation.cleanup();
        disableListener();
    }

    private void observeRingAngle(float angle) {
        binding.compass.setRingAngle(-angle);
    }

    private void observeNorthPointer(float angle) {
        binding.compass.setNorthPointerAngle(-angle);
    }

    private void observeDirection(String direction) {
        binding.headline.setText(direction);
    }

    private void observeManualMode(boolean manualMode) {
        if (manualMode) {
            buttonAnimation.show();
        } else {
            buttonAnimation.dismiss();
        }
    }

    private void getLookAtPhoneFromAboveLD(boolean lookAtPhoneFromAbove) {
        binding.compass.setMirror(!lookAtPhoneFromAbove);
    }

    private void displayHints() {
        binding.hintsHeader.setText(getHintsHeader());
        binding.hints.setText(getHints());
        hintsAnimation.show();
    }

    private CharSequence getHintsHeader() {
        return getText(viewModel.isManual() ? R.string.label_manual_mode : R.string.label_automatic_mode);
    }

    private CharSequence getHints() {
        return getText(viewModel.isManual() ? R.string.text_hints_manual_mode : R.string.text_hints_automatic_mode);
    }

    private void dismissHints() {
        hintsAnimation.dismiss();
    }

    private void onSettings() {
        NavController navController = Navigation.findNavController(binding.getRoot());
        NavDirections directions = MainFragmentDirections.actionMainFragmentToSettingsFragment();
        navController.navigate(directions);
    }

    private void onObserverSettings(Settings settings) {
        binding.magnetometer.setVisibility(settings.showMagnetometer ? View.VISIBLE : View.INVISIBLE);
        binding.accelerometer.setVisibility(settings.showAccelerometer ? View.VISIBLE : View.INVISIBLE);
        if (settings.showMagnetometer || settings.showAccelerometer) {
            enableListener();
        } else {
            disableListener();
        }
    }

    private void enableListener() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {

            float[] accelerometer = null;
            float[] magnetometer = null;
            final Settings settings = viewModel.getSettings();

            @Override
            public void run() {
                if (settings.showAccelerometer) {
                    float[] values = sensorListener.getAccelerometer();
                    // Acceleration along x, y, z axis, sin(alpha) = x / 9.81 and sin(beta) = y / 9.81
                    if (settings.applyLowPassFilter && accelerometer != null) {
                        accelerometer[0] += 0.2f * (values[0] - accelerometer[0]);
                        accelerometer[1] += 0.2f * (values[1] - accelerometer[1]);
                        accelerometer[2] += 0.2f * (values[2] - accelerometer[2]);
                    } else {
                        accelerometer = new float[3];
                        accelerometer[0] = values[0];
                        accelerometer[1] = values[1];
                        accelerometer[2] = values[2];
                    }
                    if (Math.abs(accelerometer[2]) > 0.001) {
                        double alpha = Degree.arcSin(accelerometer[0] / 9.81);
                        double beta = -Degree.arcSin(accelerometer[1] / 9.81);
                        binding.accelerometer.setTranslationX(10f * (float)alpha);
                        binding.accelerometer.setTranslationY(10f * (float)beta);
                    }
                }
                if (viewModel.getSettings().showMagnetometer) {
                    // Magnetic field strength along x, y, z axis --> tan(alpha) = x / y
                    float[] values = sensorListener.getMagnetometer();
                    if (settings.applyLowPassFilter && magnetometer != null) {
                        magnetometer[0] += 0.2f * (values[0] - magnetometer[0]);
                        magnetometer[1] += 0.2f * (values[1] - magnetometer[1]);
                        magnetometer[2] += 0.2f * (values[2] - magnetometer[2]);
                    } else {
                        magnetometer = new float[3];
                        magnetometer[0] = values[0];
                        magnetometer[1] = values[1];
                        magnetometer[2] = values[2];
                    }
                    if (Math.abs(magnetometer[0]) > 0.001 || Math.abs(magnetometer[1]) > 0.001) {
                        double alpha = Degree.arcTan2(magnetometer[0], magnetometer[1]);
                        binding.magnetometer.setRotation((float) alpha);
                    }
                }

                handler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void disableListener() {
        binding.magnetometer.setVisibility(View.INVISIBLE);
        binding.accelerometer.setVisibility(View.INVISIBLE);
        handler.removeCallbacksAndMessages(null);
    }
}





