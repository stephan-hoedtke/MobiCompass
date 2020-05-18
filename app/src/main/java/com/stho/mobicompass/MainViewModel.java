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

    @SuppressWarnings("ConstantConditions")
    static MainViewModel build(@NonNull Fragment fragment) {
        return new ViewModelProvider(fragment.getActivity()).get(MainViewModel.class);
    }

    private final MutableLiveData<Double> northPointerPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> ringAngleLiveData = new MutableLiveData<>();
    private Acceleration acceleration;
    private LowPassFilter lowPassFilter;

    LiveData<Float> getNorthPointerAngleLD() { return Transformations.map(northPointerPositionLiveData, alpha -> (float)Points.toDegree(alpha)); }
    LiveData<String> getDirectionLD() { return Transformations.map(northPointerPositionLiveData, Points::getName); }
    LiveData<Float> getRingAngleLD() { return ringAngleLiveData; }

    private void initialize() {
        acceleration = new Acceleration();
        lowPassFilter = new LowPassFilter();
        northPointerPositionLiveData.postValue(0.0);
        ringAngleLiveData.postValue(0.0f);
    }

    @SuppressWarnings("ConstantConditions")
    void rotate(double delta) {
        double angle = Degree.normalize(ringAngleLiveData.getValue() + delta);
        ringAngleLiveData.postValue((float)angle);
    }

    void reset() {
        ringAngleLiveData.postValue(0f);
    }

    void seek() {
        ringAngleLiveData.postValue((float)acceleration.getPosition());
    }

    void updateNorthPointer() {
        northPointerPositionLiveData.postValue(acceleration.getPosition());
    }

    void update(float[] orientationAngles) {
        Vector gravity = lowPassFilter.setAcceleration(orientationAngles);
        update(gravity.x, Angle.normalizePlusMinus(gravity.z));
    }

    private static final double PI90 = 0.5 * Math.PI;

    private void update(double azimuth, double roll) {
        if (roll < -PI90 || roll > PI90) {
            azimuth = Angle.normalize(0 - azimuth); // look at screen from below
        }
        updateAcceleration(azimuth);
    }

    /*
        Determine, if we need to rotate clockwise or anti-clockwise
     */
    private void updateAcceleration(double newAngle) {
        double angle = acceleration.getPosition();
        acceleration.update(Angle.rotateTo(angle, newAngle));
    }
}
