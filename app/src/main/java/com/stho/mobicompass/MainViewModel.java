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

    LiveData<Float> getNorthPointerPositionLD() { return northPointerPositionLiveData; }
    LiveData<String> getDirectionNameLD() { return Transformations.map(northPointerPositionLiveData, Direction::getName); }
    LiveData<Float> getRingAngleLD() { return ringAngleLiveData; }

    void rotateRing(double deltaInDegree) {
        double degree = Degree.normalize(ringAngleLiveData.getValue() + deltaInDegree);
        ringAngleLiveData.postValue((float)degree);
    }

    void reset() {
        ringAngleLiveData.postValue(0f);
    }

    void seek() {
        double degree = northPointerPositionLiveData.getValue();
        ringAngleLiveData.postValue((float)-degree);
    }

    void updateNorthPointer(Orientation orientation) {
        double azimuth = orientation.getAzimuth();
        northPointerPositionLiveData.postValue((float)azimuth);
    }
}

