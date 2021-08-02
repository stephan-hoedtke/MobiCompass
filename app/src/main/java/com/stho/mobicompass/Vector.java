package com.stho.mobicompass;


/**
 * Created by shoedtke on 23.12.2015.
 */
@SuppressWarnings("WeakerAccess")
public class Vector {
    public final Double x;
    public final Double y;
    public final Double z;

    public static Vector defaultValue() { return new Vector(0.0, 0.0, 0.0); }

    public Vector(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector normalize() {
        return this.div(norm());
    }

    public double norm() {
        return Math.sqrt(normSquare());
    }

    public double normSquare() {
        return x * x + y * y + z * z;
    }

    public Vector cross(Vector v) {
        return Vector.cross(this, v);
    }

    public double dot(Vector v) {
        return Vector.dot(this, v);
    }

    public Vector times(double f) {
        return new Vector(
                f * x,
                f * y,
                f * z);
    }

    public Vector div(double f) {
        return new Vector(
                x / f,
                y / f,
                z / f);
    }

    public static Vector fromFloatArray(float[] array) {
        return new Vector(
                (double)array[0],
                (double)array[1],
                (double)array[2]);
    }

    private static double dot(Vector a, Vector b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    private static Vector cross(Vector a, Vector b) {
        return new Vector(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x);
    }
}



