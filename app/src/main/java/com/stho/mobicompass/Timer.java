package com.stho.mobicompass;

public class Timer {
    private final SystemClockTimeSource timeSource = new SystemClockTimeSource();
    private double startTime = timeSource.getElapsedRealtimeSeconds();

    /**
     * Reset start time
     */
    public void reset() {
        startTime = timeSource.getElapsedRealtimeSeconds();
    }

    /**
     * Return elapsed time (since last start time) in seconds
     */
    public double getTime() {
        return timeSource.getElapsedRealtimeSeconds() - startTime;
    }

    /**
     * Return elapsed time (since last start time) in seconds and reset start time
     *
     *      getTime() + reset()
     */
    public double getNextTime() {
        double previousStartTime = startTime;
        startTime = timeSource.getElapsedRealtimeSeconds();
        return startTime - previousStartTime;
    }
}

