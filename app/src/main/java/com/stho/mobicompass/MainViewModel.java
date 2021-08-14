package com.stho.mobicompass;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Float> ringAngleLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> manualModeLiveData = new MutableLiveData<>();
    private final OrientationFilter orientationFilter = new OrientationFilter();
    private final MediatorLiveData<Float> mediator = new MediatorLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        ringAngleLiveData.setValue(0f);
        manualModeLiveData.setValue(false);
        mediator.addSource(orientationFilter.getOrientationLD(),
                orientation -> {
                    float value = getNorthPointerPositionFromOrientation(orientation);
                    mediator.setValue(value);
                    if (isAutomaticMode()) {
                        ringAngleLiveData.setValue(value);
                    }
                });
    }

    static MainViewModel build(@NonNull Fragment fragment) {
        return new ViewModelProvider(fragment.requireActivity()).get(MainViewModel.class);
    }

    LiveData<Float> getNorthPointerPositionLD() { return mediator; }
    LiveData<Float> getRingAngleLD() { return ringAngleLiveData; }
    LiveData<Boolean> getManualModeLD() { return manualModeLiveData; }
    LiveData<String> getDirectionNameLD() { return Transformations.map(getRingAngleLD(), Direction::getName); }
    OrientationFilter getOrientationFilter() { return orientationFilter; }

    /**
     * Synchronous, call from UI thread only
     * @param deltaInDegree delta rotation angle in degree
     */
    void rotateRing(double deltaInDegree) {
        double degree = Degree.normalize(assureValueOrAssumeZero(ringAngleLiveData.getValue()) - deltaInDegree);
        manualModeLiveData.setValue(true);
        ringAngleLiveData.setValue((float)degree);
    }

    void reset() {
        manualModeLiveData.setValue(true);
        ringAngleLiveData.setValue(0f);
    }

    void toggleManualMode() {
        manualModeLiveData.setValue(isAutomaticMode());
    }

    private boolean isAutomaticMode() {
        return !assureValueOrAssumeTrue(manualModeLiveData.getValue());
    }

    private static float assureValueOrAssumeZero(Float value) {
        return (value == null) ? 0f : value;
    }

    private static boolean assureValueOrAssumeTrue(Boolean value) {
        return (value != null) && value;
    }

    private static float getNorthPointerPositionFromOrientation(Orientation orientation) {
        double azimuth = orientation.getAzimuth();
        double roll = orientation.getRoll();
        if (-90 <= roll && roll <= 90) {
            return (float)azimuth;
        }
        else {
            return - (float)azimuth;
        }
    }

}

