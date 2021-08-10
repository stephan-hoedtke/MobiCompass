package com.stho.mobicompass;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

class OrientationFilter implements OrientationSensorListener.IOrientationFilter {

    private final MutableLiveData<Quaternion> orientationQuaternionLiveData = new MutableLiveData<>();

    public LiveData<Orientation> getOrientationLD() {
        return Transformations.map(orientationQuaternionLiveData, Quaternion::toOrientation);
    }

    @Override
    public void onOrientationAnglesChanged(Quaternion newOrientation) {
        Quaternion currentOrientation = orientationQuaternionLiveData.getValue();
        if (currentOrientation == null || areDifferent(currentOrientation, newOrientation)) {
            orientationQuaternionLiveData.postValue(newOrientation);
        }
    }

    private static boolean areDifferent(Quaternion a, Quaternion b) {
        return areDifferent(a.x, b.x) || areDifferent(a.y, b.y) ||areDifferent(a.z, b.z)|| areDifferent(a.s, b.s);
    }

    private static boolean areDifferent(double a, double b) {
        return Math.abs(a - b) > EPS;
    }

    private final static double EPS = 0.000000001;
}
