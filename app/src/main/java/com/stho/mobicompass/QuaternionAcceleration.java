package com.stho.mobicompass;

public class QuaternionAcceleration {

        private final TimeSource timeSource;
        private final double delta = 5.0;
        private final double factor;
        private double v0 = 0.0;
        private double x0 = 0.0;
        private double t0;
        private Quaternion q0 = Quaternion.defaultValue();
        private Quaternion q1 = Quaternion.defaultValue();

        private QuaternionAcceleration(double factorInSeconds, TimeSource timeSource) {
                this.timeSource = timeSource;
                this.factor = 1 / factorInSeconds;
                this.t0 = timeSource.getElapsedRealtimeSeconds();
        }

        public static QuaternionAcceleration create() {
                return new QuaternionAcceleration(0.25, new SystemClockTimeSource());
        }

        public static QuaternionAcceleration create(double factorInSeconds) {
                return new QuaternionAcceleration(factorInSeconds, new SystemClockTimeSource());
        }

        public static QuaternionAcceleration create(double factorInSeconds, TimeSource timeSource) {
                return new QuaternionAcceleration(factorInSeconds, timeSource);
        }

        public Quaternion getPosition() {
                double t = getTime(timeSource.getElapsedRealtimeSeconds());
                return getPosition(t);
        }

        public void rotateTo(Quaternion targetQuaternion) {
                double t1 = timeSource.getElapsedRealtimeSeconds();
                double t = getTime(t1);
                double v = getSpeed(t);
                q0 = getPosition(t);
                q1 = targetQuaternion;
                x0 = 1.0;
                v0 = v;
                t0 = t1;
        }

        private double getTime(double t1) {
                return factor * (t1 - t0);
        }

        /**
         * Interpolation of two quaternions so that:
         * t = 0 --> x(t) = 1 --> q0
         * t > 2 --> x(t) = 0 --> q1
         * <p>
         * Do not use the simple linear interpolation, as it fails for dot(q0, q1) < 0
         * q := q1 + (q0 - q1) * x(t)
         */
        private Quaternion getPosition(double t) {
                return Quaternion.interpolate(q1, q0, x(t));
        }

        private double getSpeed(double t) {
                return v(t);
        }

        /**
         * moves the position x from x0 at time 0 to 0 at time 2
         */
        private double x(double t) {
                if (t < 0)
                        return x0;
                else if (t > 2)
                        return 0.0;
                else
                        return (x0 + (x0 * delta + v0) * t) * Math.exp(-delta * t);
        }

        /**
         * reduces the speed from v0 at time 0 to 0 at time 2
         */
        private double v(double t) {
                if (t < 0)
                        return v0;
                else if (t > 2)
                        return 0.0;
                else
                        return (v0 - (v0 + x0 * delta) * delta * t) * Math.exp(-delta * t);
        }
}
