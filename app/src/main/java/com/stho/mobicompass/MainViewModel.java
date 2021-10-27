package com.stho.mobicompass;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Float> ringAngleLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> manualModeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lookAtPhoneFromAboveLiveData = new MutableLiveData<>();
    private final MutableLiveData<Settings> settingsLiveData = new MutableLiveData<>();
    private final OrientationFilter orientationFilter = new OrientationFilter();
    private final MediatorLiveData<Float> northPointerAngleMediatorLiveData = new MediatorLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        ringAngleLiveData.setValue(0f);
        manualModeLiveData.setValue(false);
        lookAtPhoneFromAboveLiveData.setValue(true);
        settingsLiveData.setValue(Settings.defaultValue());
        northPointerAngleMediatorLiveData.addSource(orientationFilter.getOrientationLD(),
                orientation -> {
                    float northPointerAngle = getNorthPointerPositionFromOrientation(orientation);
                    boolean lookAtThePhoneFromAbove = lookAtPhoneFromAboveFromOrientation(orientation);
                    setNorthPointerAngle(northPointerAngle);
                    setLookAtPhoneFromAbove(lookAtThePhoneFromAbove);
                    if (isAutomaticMode()) {
                        setRingAngle(northPointerAngle);
                    }
                });
    }

    static MainViewModel build(@NonNull FragmentActivity activity) {
        return new ViewModelProvider(activity).get(MainViewModel.class);
    }

    LiveData<Float> getNorthPointerPositionLD() { return northPointerAngleMediatorLiveData; }
    LiveData<Float> getRingAngleLD() { return ringAngleLiveData; }
    LiveData<Boolean> getManualModeLD() { return manualModeLiveData; }
    LiveData<Boolean> getLookAtPhoneFromAboveLD() { return lookAtPhoneFromAboveLiveData; }
    LiveData<Settings> getSettingsLD() { return settingsLiveData; }
    OrientationFilter getOrientationFilter() { return orientationFilter; }

    LiveData<String> getDirectionNameLD() {
        return Transformations.map(ringAngleLiveData, Direction::getName);
    }

    @NonNull Settings getSettings() {
        return Objects.requireNonNull(settingsLiveData.getValue());
    }

    void setShowMagnetometer(boolean value) {
        Settings settings = getSettings();
        if (settings.showMagnetometer != value) {
            settingsLiveData.postValue(new Settings(value, settings.showAccelerometer, settings.applyLowPassFilter));
        }
    }

    void setShowAccelerometer(boolean value) {
        Settings settings = getSettings();
        if (settings.showAccelerometer != value) {
            settingsLiveData.postValue(new Settings(settings.showMagnetometer, value, settings.applyLowPassFilter));
        }
    }

    void setApplyLowPassFilter(boolean value) {
        Settings settings = getSettings();
        if (settings.showAccelerometer != value) {
            settingsLiveData.postValue(new Settings(settings.showMagnetometer, settings.showAccelerometer, value));
        }
    }

    /**
     * Synchronous, call from UI thread only
     * @param deltaInDegree delta rotation angle in degree
     */
    void rotateRing(double deltaInDegree) {
        double degree = Degree.normalize(assureValueOrAssumeZero(ringAngleLiveData.getValue()) - deltaInDegree);
        manualModeLiveData.setValue(true);
        ringAngleLiveData.setValue((float)degree);
    }

    void setAutomaticMode() {
        manualModeLiveData.setValue(false);
    }

    boolean isManual() {
        Boolean value = getManualModeLD().getValue();
        return value == null || value;
    }

    void fix() {
        Float angle = northPointerAngleMediatorLiveData.getValue();
        manualModeLiveData.setValue(true);
        ringAngleLiveData.setValue(angle);
    }

    private void setNorthPointerAngle(float newValue) {
        Float value = northPointerAngleMediatorLiveData.getValue();
        if (value == null || value != newValue) {
            northPointerAngleMediatorLiveData.setValue(newValue);
        }
    }

    private void setLookAtPhoneFromAbove(boolean newValue) {
        Boolean value = lookAtPhoneFromAboveLiveData.getValue();
        if (value == null || value != newValue) {
            lookAtPhoneFromAboveLiveData.setValue(newValue);
        }
    }

    private void setRingAngle(float newValue) {
        Float value = ringAngleLiveData.getValue();
        if (value == null || value != newValue) {
            ringAngleLiveData.setValue(newValue);
        }
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
        return (float)orientation.getAzimuth();
    }

    private static boolean lookAtPhoneFromAboveFromOrientation(Orientation orientation) {
        double roll = orientation.getRoll();
        return -90 <= roll && roll <= 90;
    }
}

