package com.stho.mobicompass;

public class RotationMatrix {

    public final double m11;
    public final double m12;
    public final double m13;
    public final double m21;
    public final double m22;
    public final double m23;
    public final double m31;
    public final double m32;
    public final double m33;

    RotationMatrix(
            double m11, double m12, double m13,
            double m21, double m22, double m23,
            double m31, double m32, double m33) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public static RotationMatrix fromFloatArray(float[] m) {
        return new RotationMatrix(
                m[0],
                m[1],
                m[2],
                m[3],
                m[4],
                m[5],
                m[6],
                m[7],
                m[8]);
    }
}




