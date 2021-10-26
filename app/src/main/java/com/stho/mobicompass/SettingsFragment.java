package com.stho.mobicompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toolbar;

import com.stho.mobicompass.databinding.MainFragmentBinding;
import com.stho.mobicompass.databinding.SettingsFragmentBinding;

public class SettingsFragment extends Fragment {

    private MainViewModel viewModel;
    private SettingsFragmentBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = MainViewModel.build(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentBinding.inflate(inflater, container, false);
        binding.version.setText(BuildConfig.VERSION_NAME);
        binding.switchShowMagnetometer.setOnCheckedChangeListener((compoundButton, value) -> viewModel.setShowMagnetometer(value));
        binding.switchShowAccelerometer.setOnCheckedChangeListener((compoundButton, value) -> viewModel.setShowAccelerometer(value));
        binding.buttonBack.setOnClickListener(view -> onHome());
        binding.buttonPdf.setOnClickListener(view -> onPdf());
        binding.valueLambda1.setText(getString(R.string.value, FastAHRSFilter.LAMBDA1));
        binding.valueLambda2.setText(getString(R.string.value, FastAHRSFilter.LAMBDA2));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getSettingsLD().observe(getViewLifecycleOwner(), this::onObserveSettings);
    }

    private void onObserveSettings(Settings settings) {
        binding.switchShowAccelerometer.setChecked(settings.showAccelerometer);
        binding.switchShowMagnetometer.setChecked(settings.showMagnetometer);
    }

    private void onHome() {
        NavController navController = Navigation.findNavController(binding.getRoot());
        navController.navigateUp();
    }

    private void onPdf() {
        DocumentationFileAdapter adapter = new DocumentationFileAdapter(requireContext());
        adapter.openPdf("SeparatedCorrectionFilter.pdf");
    }
}


