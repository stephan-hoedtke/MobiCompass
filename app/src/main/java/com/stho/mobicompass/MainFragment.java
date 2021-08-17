package com.stho.mobicompass;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.stho.mobicompass.databinding.MainFragmentBinding;

public class MainFragment extends Fragment {

    private MainViewModel viewModel;
    private MainFragmentBinding binding;
    private OrientationSensorListener sensorListener;
    private ButtonAnimation buttonAnimation;
    private HintsAnimation hintsAnimation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = MainViewModel.build(requireActivity());
        sensorListener = new OrientationSensorListener(requireContext(), viewModel.getOrientationFilter());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
        binding.compass.setOnRotateListener(delta -> viewModel.rotateRing(delta));
        binding.compass.setOnDoubleTapListener(() -> viewModel.fix());
        binding.buttonManualMode.setOnClickListener(view -> viewModel.toggleManualMode());
        binding.buttonShowHints.setOnClickListener(view -> displayHints());
        binding.buttonDismissHints.setOnClickListener(view -> dismissHints());
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
        buttonAnimation = ButtonAnimation.build(binding.buttonManualMode);
        hintsAnimation = HintsAnimation.build(binding.hintsFrame);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorListener.onResume();
        hintsAnimation.hide();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorListener.onPause();
        hintsAnimation.cleanup();
        buttonAnimation.cleanup();
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
        CharSequence hints = getText(viewModel.isManual() ? R.string.label_hints_manual_mode : R.string.label_hints_automatic_mode);
        binding.hints.setText(hints);
        hintsAnimation.show();
    }

    private void dismissHints() {
        hintsAnimation.dismiss();
    }

    private static final int SNACKBAR_DURATION = 13000;


}




