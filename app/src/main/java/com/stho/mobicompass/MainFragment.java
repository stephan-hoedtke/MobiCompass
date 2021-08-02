package com.stho.mobicompass;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.stho.mobicompass.databinding.MainFragmentBinding;

public class MainFragment extends Fragment {

    private final Handler handler = new Handler();
    private MainViewModel viewModel;
    private MainFragmentBinding binding;
    private OrientationSensorListener.IOrientationFilter filter;
    private OrientationSensorListener sensorListener ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = MainViewModel.build(this);
        filter = new OrientationAccelerationFilter();
        sensorListener = new OrientationSensorListener(requireContext(), filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
        binding.compassRing.setOnRotateListener(delta -> viewModel.rotateRing(delta));
        binding.compassRing.setOnDoubleTapListener(() -> viewModel.reset());
        binding.buttonManualMode.setOnClickListener(view -> viewModel.toggleManualMode());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getRingAngleLD().observe(getViewLifecycleOwner(), this::observeRingAngle);
        viewModel.getNorthPointerPositionLD().observe(getViewLifecycleOwner(), this::observeNorthPointer);
        viewModel.getDirectionNameLD().observe(getViewLifecycleOwner(), this::observeDirection);
        viewModel.getManualModeLD().observe(getViewLifecycleOwner(), this::observeManualMode);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorListener.onResume();
        initializeHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorListener.onPause();
        removeHandler();
    }

    private static final int HANDLER_DELAY = 100;

    private void initializeHandler() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Orientation orientation = filter.getCurrentOrientation();
                viewModel.updateNorthPointer(orientation);
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, HANDLER_DELAY);
    }

    private void removeHandler() {
        handler.removeCallbacksAndMessages(null);
    }

    private void observeRingAngle(float angle) {
        binding.compassRing.setRotation(-angle);
    }

    private void observeNorthPointer(float angle) {
        binding.compassNorthPointer.setRotation(-angle);
    }

    private void observeDirection(String direction) {
        binding.headline.setText(direction);
    }

    private void observeManualMode(boolean manualMode) {
        if (manualMode) {
            handler.postDelayed(this::fadeInAutoIcon, 10);
        } else {
            handler.postDelayed(this::fadeOutAutoIcon, 300);
        }
    }

    private void fadeInAutoIcon() {
        binding.buttonManualMode.setVisibility(View.VISIBLE);
        binding.buttonManualMode.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
    }

    private void fadeOutAutoIcon() {
        binding.buttonManualMode.setVisibility(View.VISIBLE);
        binding.buttonManualMode.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.buttonManualMode.setVisibility(View.INVISIBLE);
                    }
                });
    }
}




