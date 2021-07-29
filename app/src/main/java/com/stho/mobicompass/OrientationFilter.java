package com.stho.mobicompass;


/*
    The class takes updates of the orientation vector by listening to onOrientationChanged(rotationMatrix).
    The values will be stored and smoothed with acceleration.
    A handler will regularly read the updated smoothed orientation
 */
class OrientationAccelerationFilter implements OrientationSensorListener.IOrientationFilter {

    private final QuaternionAcceleration acceleration = QuaternionAcceleration.create(DEFAULT_ACCELERATION_FACTOR);

    @Override
    public Orientation getCurrentOrientation() {
        Quaternion position = acceleration.getPosition();
        Orientation orientation = position.toOrientation();
        return orientation.normalize();
    }

    @Override
    public void onOrientationAnglesChanged(Quaternion orientation) {
        acceleration.rotateTo(orientation);
    }

    private static final double DEFAULT_ACCELERATION_FACTOR = 0.25;
}
