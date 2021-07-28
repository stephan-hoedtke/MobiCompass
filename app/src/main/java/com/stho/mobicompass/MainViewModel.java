package com.stho.mobicompass;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;

public class MainViewModel extends AndroidViewModel {

    public MainViewModel(@NonNull Application application) {
        super(application);
        northPointerPositionLiveData.setValue(0f);
        ringAngleLiveData.setValue(0f);
    }

    static MainViewModel build(@NonNull Fragment fragment) {
        return new ViewModelProvider(fragment.requireActivity()).get(MainViewModel.class);
    }

    private final MutableLiveData<Float> northPointerPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> ringAngleLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> manualModeLiveData = new MutableLiveData<>();

    LiveData<Float> getNorthPointerPositionLD() { return northPointerPositionLiveData; }
    LiveData<String> getDirectionNameLD() { return Transformations.map(ringAngleLiveData, Direction::getName); }
    LiveData<Float> getRingAngleLD() { return ringAngleLiveData; }
    LiveData<Boolean> getManualModeLD() { return manualModeLiveData; }

    /**
     * Synchronous, call from UI thread only
     * @param deltaInDegree
     */
    void rotateRing(double deltaInDegree) {
        double degree = Degree.normalize(assureValue(ringAngleLiveData.getValue()) - deltaInDegree);
        manualModeLiveData.setValue(true);
        ringAngleLiveData.setValue((float)degree);
    }

    void reset() {
        manualModeLiveData.setValue(false);
    }

    void seek() {
        manualModeLiveData.setValue(false);
    }

    void updateNorthPointer(Orientation orientation) {
        double azimuth = orientation.getAzimuth();
        northPointerPositionLiveData.postValue((float)azimuth);

        if (isAutomaticMode()) {
            ringAngleLiveData.postValue((float)azimuth);
        }
    }

    private boolean isAutomaticMode() {
        return !assureValue(manualModeLiveData.getValue());
    }

    private static float assureValue(Float value) {
        return (value == null) ? (float) 0.0 : value;
    }

    private static boolean assureValue(Boolean value) {
        return (value != null) && value;
    }

}

