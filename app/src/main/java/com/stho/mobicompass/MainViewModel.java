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
        initialize();
    }

    static MainViewModel build(@NonNull Fragment fragment) {
        return new ViewModelProvider(fragment.getActivity()).get(MainViewModel.class);
    }

    private final MutableLiveData<Double> northPointerPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> ringAngleLiveData = new MutableLiveData<>();
    private Acceleration acceleration;
    private LowPassFilter lowPassFilter;

    LiveData<Float> getNorthPointerPositionLD() { return Transformations.map(northPointerPositionLiveData, angle -> (float)Angle.toDegree(angle)); }
    LiveData<String> getDirectionNameLD() { return Transformations.map(northPointerPositionLiveData, Direction::getName); }
    LiveData<Float> getRingAngleLD() { return ringAngleLiveData; }

    private void initialize() {
        acceleration = new Acceleration();
        lowPassFilter = new LowPassFilter();
        northPointerPositionLiveData.postValue(0.0);
        ringAngleLiveData.postValue(0.0f);
    }

    void rotate(double delta) {
        double angle = Degree.normalize(ringAngleLiveData.getValue() + delta);
        ringAngleLiveData.postValue((float)angle);
    }

    void reset() {
        ringAngleLiveData.postValue(0f);
    }

    void seek() {
        float degree = (float)Angle.toDegree(acceleration.getPosition());
        ringAngleLiveData.postValue(-degree);
    }

    void updateNorthPointer() {
        northPointerPositionLiveData.postValue(acceleration.getPosition());
    }

    void update(float[] orientationAngles) {
        Vector gravity = lowPassFilter.setAcceleration(orientationAngles);
        update(gravity.x, Angle.normalizePlusMinusPI(gravity.z));
    }

    private static final double PI_90 = 0.5 * Math.PI;

    private void update(double azimuth, double roll) {
        if (roll < -PI_90 || roll > PI_90) {
            azimuth = Angle.normalize(0 - azimuth); // look at screen from below
        }
        updateAcceleration(azimuth);
    }

    private void updateAcceleration(double newAngle) {
        acceleration.rotateTo(newAngle);
    }
}
