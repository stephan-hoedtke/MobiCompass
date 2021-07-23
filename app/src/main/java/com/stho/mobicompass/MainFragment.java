package com.stho.mobicompass;

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
    private IOrientationFilter filter;
    private OrientationSensorListener sensorListener ;
    public static MainFragment build() {
        return new MainFragment();
    }

    @SuppressWarnings("ConstantConditions")
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
        binding.compassRing.setOnDoubleTapListener(() -> viewModel.seek());
        binding.headline.setOnClickListener(view -> viewModel.reset());
        viewModel.getRingAngleLD().observe(getViewLifecycleOwner(), alpha -> binding.compassRing.setRotation(alpha));
        viewModel.getNorthPointerPositionLD().observe(getViewLifecycleOwner(), angle -> {
            binding.compassNorthPointer.setRotation(-angle);
            binding.headline.setText(Formatter.toString0(angle));
        });
        viewModel.getDirectionNameLD().observe(getViewLifecycleOwner(), direction -> binding.headline.setText(direction));
        return binding.getRoot();
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
}


