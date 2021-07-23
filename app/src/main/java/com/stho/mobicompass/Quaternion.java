package com.stho.mobicompass;


/**
 * https://mathworld.wolfram.com/Quaternion.html
 * https://www.ashwinnarayan.com/post/how-to-integrate-quaternions/
 */
class Quaternion {

    public final double x;
    public final double y;
    public final double z;
    public final double s;

    public Quaternion(double x, double y, double z, double s) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.s = s;
    }

    // https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/


    public Orientation toOrientation() {
        return Rotation.getOrientationFor(toRotationMatrix());
    }

    public Quaternion plus(Quaternion q) {
        return new Quaternion(
                this.x + q.x,
                this.y + q.y,
                this.z + q.z,
                this.s + q.s);
    }

    public Quaternion minus(Quaternion q) {
        return new Quaternion(
                this.x - q.x,
                this.y - q.y,
                this.z - q.z,
                this.s - q.s);
    }

    public Quaternion times(double f) {
        return new Quaternion(
                this.x * f,
                this.y * f,
                this.z * f,
                this.s * f);
    }

    public Quaternion div(double f) {
        return new Quaternion(
                this.x / f,
                this.y / f,
                this.z / f,
                this.s / f);
    }


    public Quaternion times(Quaternion q) {
        return Quaternion.hamiltonProduct(this, q);
    }

    public double norm() {
        return Math.sqrt(normSquare());
    }

    public double normSquare() {
        return x * x + y * y + z * z + s * s;
    }

    public Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, s);
    }

    public Quaternion inverse() {
        return conjugate().div(norm());
    }

    public Quaternion normalize() {
        return this.div(norm());
    }

    /**
     * Quaternion for rotating by theta (in radians) around the vector (x, y, z)
     */
    public static Quaternion forRotation(double x, double y, double z, double theta) {
        return forRotation(new Vector(x, y, z), theta);
    }

    /**
     * Quaternion for rotating by theta (in radians) around the vector u
     */
    public static Quaternion forRotation(Vector u, double theta) {
        // see: https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
        double thetaOverTwo = theta / 2.0;
        double sinThetaOverTwo = Math.sin(thetaOverTwo);
        double cosThetaOverTwo = Math.cos(thetaOverTwo);
        return new Quaternion(
                u.x * sinThetaOverTwo,
                u.y * sinThetaOverTwo,
                u.z * sinThetaOverTwo,
                cosThetaOverTwo);
    }

    public static Quaternion defaultValue() {
        return new Quaternion(0.0, 0.0, 0.0, 1.0);
    }


    // (r1,v1) * (r2,v2) = (r1 r2 - dot(v1,v2), r1 v2 + r2 v1 + cross(v1, v2)
    private static Quaternion hamiltonProduct(Quaternion a, Quaternion b) {
        double a1 = a.s;
        double b1 = a.x;
        double c1 = a.y;
        double d1 = a.z;
        double a2 = b.s;
        double b2 = b.x;
        double c2 = b.y;
        double d2 = b.z;
        return new Quaternion(
                a1 * b2 + b1 * a2 + c1 * d2 - d1 * c2,
                a1 * c2 + c1 * a2 - b1 * d2 + d1 * b2,
                a1 * d2 + d1 * a2 + b1 * c2 - c1 * b2,
                a1 * a2 - b1 * b2 - c1 * c2 - d1 * d2);
    }

    public static Quaternion fromRotationMatrix(RotationMatrix m) {
        // see: https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
        // mind, as both q and -q define the same rotation you may get q or -q, respectively
        double m11 = m.m11;
        double m12 = m.m12;
        double m13 = m.m13;
        double m21 = m.m21;
        double m22 = m.m22;
        double m23 = m.m23;
        double m31 = m.m31;
        double m32 = m.m32;
        double m33 = m.m33;
        if (m11 + m22 + m33 > 0) {
            double fourS = 2.0 * Math.sqrt(1.0 + m11 + m22 + m33); // 4s = 4 * q.s
            return new Quaternion(
                    (m32 - m23) / fourS,
                    (m13 - m31) / fourS,
                    (m21 - m12) / fourS,
                    0.25 * fourS);
        } else if (m11 > m22 && m11 > m33) {
            double fourX = 2.0 * Math.sqrt(1.0 + m11 - m22 - m33); // 4x = 4 * q.x
            return new Quaternion(
                    0.25 * fourX,
                    (m12 + m21) / fourX,
                    (m13 + m31) / fourX,
                    (m32 - m23) / fourX);
        } else if (m22 > m33) {
            double fourY = 2.0 * Math.sqrt(1.0 + m22 - m11 - m33); // 4y = 4*q.y
            return new Quaternion(
                    (m12 + m21) / fourY,
                    0.25 * fourY,
                    (m23 + m32) / fourY,
                    (m13 - m31) / fourY);
        } else {
            double fourZ = 2.0 * Math.sqrt(1.0 + m33 - m11 - m22); // 4z = 4 * q.z
            return new Quaternion(
                    (m13 + m31) / fourZ,
                    (m23 + m32) / fourZ,
                    0.25 * fourZ,
                    (m21 - m12) / fourZ);
        }
    }

    public RotationMatrix toRotationMatrix() {
        double x2 = 2 * x * x;
        double y2 = 2 * y * y;
        double z2 = 2 * z * z;
        double xy = 2 * x * y;
        double xz = 2 * x * z;
        double yz = 2 * y * z;
        double sz = 2 * s * z;
        double sy = 2 * s * y;
        double sx = 2 * s * x;
        return new RotationMatrix(
                1 - y2 - z2,
                xy - sz,
                xz + sy,
                xy + sz,
                1 - x2 - z2,
                yz - sx,
                xz - sy,
                yz + sx,
                1 - x2 - y2);
    }

    private static double dot(Quaternion a, Quaternion b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.s * b.s;
    }

    private static final double COS_THETA_THRESHOLD = 0.9995;

    /**
     * Q(t) := A sin((1 - t) * θ) / sin(θ) + B sin(t * θ) / sin(θ)
     * with cos(θ) = dot(A, B)
     * <p>
     * To ensure -90 <= θ <= 90: use -A when dot(A,B) < 0
     * Note:
     * Q(0) = A
     * Q(1) = B
     */
    public static Quaternion interpolate(Quaternion a, Quaternion b, double t) {
        // see: https://theory.org/software/qfa/writeup/node12.html
        // see: https://blog.magnum.graphics/backstage/the-unnecessarily-short-ways-to-do-a-quaternion-slerp/

        double cosTheta = dot(a, b);

        if (Math.abs(cosTheta) > COS_THETA_THRESHOLD) {
            // If the inputs are too close for comfort, linearly interpolate and normalize the result.
            return new Quaternion(
                    a.x + t * (b.x - a.x),
                    a.y + t * (b.y - a.y),
                    a.z + t * (b.z - a.z),
                    a.s + t * (b.s - a.s)).normalize();
        } else if (cosTheta >= 0) {
            double theta = Math.acos(cosTheta);
            double sinTheta = Math.sin(theta);
            double f1 = Math.sin((1 - t) * theta) / sinTheta;
            double f2 = Math.sin(t * theta) / sinTheta;
            return new Quaternion(
                    a.x * f1 + b.x * f2,
                    a.y * f1 + b.y * f2,
                    a.z * f1 + b.z * f2,
                    a.s * f1 + b.s * f2);
        } else {
            // Use the shorter way for -a ...
            double theta = Math.acos(-cosTheta);
            double sinTheta = Math.sin(theta);
            double f1 = Math.sin((t - 1) * theta) / sinTheta;
            double f2 = Math.sin(t * theta) / sinTheta;
            return new Quaternion(
                    a.x * f1 + b.x * f2,
                    a.y * f1 + b.y * f2,
                    a.z * f1 + b.z * f2,
                    a.s * f1 + b.s * f2);
        }
    }
}




